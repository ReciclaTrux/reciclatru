package pe.edu.upn.reciclatru.viewmodel.orders

import pe.edu.upn.reciclatru.model.orders.Order

data class OrderDetailUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val order: Order? = null,
    val loadError: String? = null,
    val cancelDialogVisible: Boolean = false,
    val cancelling: Boolean = false,
    val pendingPhotoCount: Int = 0,
    val uploadingPhotos: Boolean = false,
    val message: String? = null,
)
