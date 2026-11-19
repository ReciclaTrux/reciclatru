package pe.edu.upn.reciclatru.viewmodel.orders


import pe.edu.upn.reciclatru.model.orders.Order

import pe.edu.upn.reciclatru.model.orders.OrderAcceptance

data class OrderDetailUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val order: Order? = null,
    val loadError: String? = null,
    val cancelDialogVisible: Boolean = false,
    val cancelling: Boolean = false,
    val acceptances: List<OrderAcceptance> = emptyList(),
    val acceptancesLoading: Boolean = false,
    val acceptancesError: String? = null,
    val recyclerToChoose: OrderAcceptance? = null,
    val choosing: Boolean = false,
    val pendingPhotoCount: Int = 0,
    val uploadingPhotos: Boolean = false,
    val message: String? = null,
)
