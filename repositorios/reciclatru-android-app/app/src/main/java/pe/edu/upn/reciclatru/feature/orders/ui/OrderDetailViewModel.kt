package pe.edu.upn.reciclatru.feature.orders.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.orders.domain.GetOrderUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrder: GetOrderUseCase,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>(OrderIdArg).orEmpty()

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    fun onRefresh() {
        val current = _uiState.value
        if (current.loading || current.refreshing) return
        _uiState.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            try {
                val order = getOrder(orderId)
                _uiState.update { it.copy(refreshing = false, order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(refreshing = false, message = error.toUserMessage()) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val order = getOrder(orderId)
                _uiState.update { it.copy(loading = false, order = order) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
            }
        }
    }

    companion object {
        const val OrderIdArg = "orderId"
    }
}
