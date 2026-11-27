package pe.edu.upn.reciclatru.viewmodel.orders


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
import pe.edu.upn.reciclatru.domain.repository.orders.PendingPhotosStore
import pe.edu.upn.reciclatru.domain.usecase.orders.CancelOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.ChooseRecyclerUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderAcceptancesUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.MarkOrderCollectedUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UploadOrderPhotosUseCase
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getOrder: GetOrderUseCase,
    private val cancelOrder: CancelOrderUseCase,
    private val getAcceptances: GetOrderAcceptancesUseCase,
    private val chooseRecycler: ChooseRecyclerUseCase,
    private val uploadPhotos: UploadOrderPhotosUseCase,
    private val pendingPhotos: PendingPhotosStore,
    private val markCollected: MarkOrderCollectedUseCase,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>(OrderIdArg).orEmpty()

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        load()
        // La pantalla de editar deja su resultado en esta entrada de navegación al volver.
        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>(EditResultKey, null).collect { message ->
                if (message != null) {
                    savedStateHandle[EditResultKey] = null
                    _uiState.update { it.copy(message = message) }
                    onRefresh()
                }
            }
        }
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
                _uiState.update { it.copy(order = order) }
                refreshAcceptances(order)
                _uiState.update { it.copy(refreshing = false) }
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

    fun onCollectClick() {
        val current = _uiState.value
        if (current.collecting || current.order?.canMarkCollected != true) return
        _uiState.update { it.copy(collectDialogVisible = true) }
    }

    fun onCollectDismissed() {
        if (_uiState.value.collecting) return
        _uiState.update { it.copy(collectDialogVisible = false) }
    }

    fun onCollectConfirmed() {
        if (_uiState.value.collecting) return
        _uiState.update { it.copy(collecting = true) }
        viewModelScope.launch {
            try {
                val order = markCollected(orderId)
                _uiState.update {
                    it.copy(collecting = false, collectDialogVisible = false, order = order, message = CollectedMessage)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                val notConfirmed = (error as? NetworkException)?.code == NotConfirmedCode
                _uiState.update {
                    it.copy(
                        collecting = false,
                        collectDialogVisible = false,
                        message = if (notConfirmed) NotConfirmedMessage else error.toUserMessage(),
                    )
                }
                if (notConfirmed) onRefresh()
            }
        }
    }

    fun onRetryAcceptances() {
        val order = _uiState.value.order ?: return
        if (_uiState.value.acceptancesLoading) return
        viewModelScope.launch { refreshAcceptances(order) }
    }

    fun onChooseClick(acceptance: OrderAcceptance) {
        val current = _uiState.value
        // Un solo reciclador a la vez: con el diálogo abierto o enviando, otro toque no cuenta.
        if (current.choosing || current.recyclerToChoose != null) return
        if (current.order?.status != OrderStatus.Pending || acceptance.status != AcceptanceStatus.Accepted) return
        _uiState.update { it.copy(recyclerToChoose = acceptance) }
    }

    fun onChooseDismissed() {
        if (_uiState.value.choosing) return
        _uiState.update { it.copy(recyclerToChoose = null) }
    }

    fun onChooseConfirmed() {
        val target = _uiState.value.recyclerToChoose ?: return
        if (_uiState.value.choosing) return
        _uiState.update { it.copy(choosing = true) }
        viewModelScope.launch {
            try {
                val order = chooseRecycler(orderId, target.id)
                _uiState.update {
                    it.copy(
                        choosing = false,
                        recyclerToChoose = null,
                        order = order,
                        acceptances = emptyList(),
                        acceptancesError = null,
                        message = "Elegiste a ${target.fullName.substringBefore(' ')}",
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                val notPending = (error as? NetworkException)?.code == NotPendingCode
                _uiState.update {
                    it.copy(
                        choosing = false,
                        recyclerToChoose = null,
                        message = if (notPending) NotPendingMessage else error.toUserMessage(),
                    )
                }
                // Doble toque o pedido cancelado: la app muestra el estado actual.
                if (notPending) onRefresh()
            }
        }
    }

    fun onRetryPhotos() {
        val photos = pendingPhotos.get(orderId)
        if (_uiState.value.uploadingPhotos || photos.isEmpty()) return
        _uiState.update { it.copy(uploadingPhotos = true) }
        viewModelScope.launch {
            val failed = uploadPhotos(orderId, photos)
            val order = try {
                getOrder(orderId)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.value.order
            }
            _uiState.update {
                it.copy(
                    uploadingPhotos = false,
                    pendingPhotoCount = failed,
                    order = order,
                    message = if (failed == 0) PhotosUploadedMessage else PhotosFailedMessage,
                )
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private suspend fun refreshAcceptances(order: Order) {
        if (order.status != OrderStatus.Pending) {
            _uiState.update { it.copy(acceptances = emptyList(), acceptancesError = null) }
            return
        }
        _uiState.update { it.copy(acceptancesLoading = it.acceptances.isEmpty()) }
        try {
            val acceptances = getAcceptances(orderId)
            _uiState.update { it.copy(acceptances = acceptances, acceptancesError = null) }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            val message = error.toUserMessage()
            _uiState.update {
                if (it.acceptances.isEmpty()) it.copy(acceptancesError = message) else it.copy(message = message)
            }
        } finally {
            _uiState.update { it.copy(acceptancesLoading = false) }
        }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val order = getOrder(orderId)
                _uiState.update {
                    it.copy(loading = false, order = order, pendingPhotoCount = pendingPhotos.get(orderId).size)
                }
                refreshAcceptances(order)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
            }
        }
    }

    companion object {
        const val OrderIdArg = "orderId"
        const val EditResultKey = "order_edit_result"
        private const val NotCancellableCode = "ORDER_NOT_CANCELLABLE"
        private const val CancelledMessage = "Pedido cancelado"
        private const val PhotosUploadedMessage = "Fotos subidas"
        private const val PhotosFailedMessage = "No pudimos subir las fotos. Inténtalo de nuevo."
        private const val NotCancellableMessage = "Este pedido ya no se puede cancelar."
        private const val NotPendingCode = "ORDER_NOT_PENDING"
        private const val NotPendingMessage = "Este pedido ya no está pendiente."
        private const val NotConfirmedCode = "ORDER_NOT_CONFIRMED"
        private const val CollectedMessage = "Pedido recolectado"
        private const val NotConfirmedMessage = "Este pedido ya no está confirmado."
    }
}
