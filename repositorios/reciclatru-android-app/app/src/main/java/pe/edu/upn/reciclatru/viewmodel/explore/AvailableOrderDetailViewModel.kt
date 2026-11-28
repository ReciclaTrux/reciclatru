package pe.edu.upn.reciclatru.viewmodel.explore

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.data.remote.NetworkException
import pe.edu.upn.reciclatru.data.remote.toUserMessage
import pe.edu.upn.reciclatru.domain.repository.explore.DeviceLocationProvider
import pe.edu.upn.reciclatru.domain.usecase.explore.AcceptOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.explore.GetAvailableOrderDetailUseCase
import pe.edu.upn.reciclatru.model.explore.GeoPoint
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AvailableOrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrderDetail: GetAvailableOrderDetailUseCase,
    private val acceptOrder: AcceptOrderUseCase,
    private val locationProvider: DeviceLocationProvider,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>(OrderIdArg).orEmpty()

    private val _uiState = MutableStateFlow(AvailableOrderDetailUiState())
    val uiState: StateFlow<AvailableOrderDetailUiState> = _uiState.asStateFlow()

    private var location: GeoPoint? = null

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    fun onRefresh() {
        val current = _uiState.value
        if (current.loading || current.refreshing || current.order == null) return
        _uiState.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            try {
                val order = getOrderDetail(orderId, location)
                _uiState.update { it.copy(refreshing = false, order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(refreshing = false, message = error.toUserMessage()) }
            }
        }
    }

    fun onResume() {
        val current = _uiState.value
        if (current.loading || current.refreshing || current.order == null) return
        viewModelScope.launch {
            try {
                val order = getOrderDetail(orderId, location)
                _uiState.update { it.copy(order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            }
        }
    }

    fun onMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    fun onAcceptClick() {
        val current = _uiState.value
        if (current.accepting || current.confirmDialogVisible) return
        if (current.order == null || current.order.alreadyAccepted) return
        _uiState.update { it.copy(confirmDialogVisible = true) }
    }

    fun onAcceptDismissed() {
        if (_uiState.value.accepting) return
        _uiState.update { it.copy(confirmDialogVisible = false) }
    }

    fun onAcceptConfirmed() {
        if (_uiState.value.accepting) return
        _uiState.update { it.copy(accepting = true) }
        viewModelScope.launch {
            try {
                val order = acceptOrder(orderId)
                _uiState.update { it.copy(accepting = false, confirmDialogVisible = false, order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                when ((error as? NetworkException)?.code) {
                    NotAvailableCode -> _uiState.update {
                        it.copy(accepting = false, confirmDialogVisible = false, unavailable = true)
                    }
                    ProfileIncompleteCode -> _uiState.update {
                        it.copy(accepting = false, confirmDialogVisible = false, profileIncomplete = true)
                    }
                    else -> _uiState.update {
                        it.copy(accepting = false, confirmDialogVisible = false, message = error.toUserMessage())
                    }
                }
            }
        }
    }

    fun onProfileDialogDismissed() {
        _uiState.update { it.copy(profileIncomplete = false) }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                // Siempre se consulta al abrir; la ubicación solo calcula la distancia aproximada.
                location = locationProvider.currentLocation()
                val order = getOrderDetail(orderId, location)
                _uiState.update { it.copy(loading = false, order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                if ((error as? NetworkException)?.code == NotAvailableCode) {
                    _uiState.update { it.copy(loading = false, unavailable = true) }
                } else {
                    _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
                }
            }
        }
    }

    companion object {
        const val OrderIdArg = "orderId"
        private const val NotAvailableCode = "ORDER_NOT_AVAILABLE"
        private const val ProfileIncompleteCode = "PROFILE_INCOMPLETE"
    }
}
