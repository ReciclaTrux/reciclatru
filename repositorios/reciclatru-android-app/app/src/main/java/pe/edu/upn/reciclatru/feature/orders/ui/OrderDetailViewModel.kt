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
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.orders.domain.CancelOrderUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.GetOrderUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrder: GetOrderUseCase,
    private val cancelOrder: CancelOrderUseCase,
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

    fun onCancelClick() {
        if (_uiState.value.order?.canCancel == true) {
            _uiState.update { it.copy(cancelDialogVisible = true) }
        }
    }

    fun onCancelDismissed() {
        if (_uiState.value.cancelling) return
        _uiState.update { it.copy(cancelDialogVisible = false) }
    }

    fun onCancelConfirmed() {
        if (_uiState.value.cancelling) return
        _uiState.update { it.copy(cancelling = true) }
        viewModelScope.launch {
            try {
                val order = cancelOrder(orderId)
                _uiState.update {
                    it.copy(cancelling = false, cancelDialogVisible = false, order = order, message = CancelledMessage)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                val notCancellable = (error as? NetworkException)?.code == NotCancellableCode
                _uiState.update {
                    it.copy(
                        cancelling = false,
                        cancelDialogVisible = false,
                        message = if (notCancellable) NotCancellableMessage else error.toUserMessage(),
                    )
                }
                if (notCancellable) onRefresh()
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
        private const val NotCancellableCode = "ORDER_NOT_CANCELLABLE"
        private const val CancelledMessage = "Pedido cancelado"
        private const val NotCancellableMessage = "Este pedido ya no se puede cancelar."
    }
}
