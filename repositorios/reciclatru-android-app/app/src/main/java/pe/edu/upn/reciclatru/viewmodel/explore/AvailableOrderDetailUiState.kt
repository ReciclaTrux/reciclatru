package pe.edu.upn.reciclatru.viewmodel.explore

import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail

data class AvailableOrderDetailUiState(
    val loading: Boolean = true,
    val order: AvailableOrderDetail? = null,
    val loadError: String? = null,
    val confirmDialogVisible: Boolean = false,
    val accepting: Boolean = false,
    val profileIncomplete: Boolean = false,
    /** El pedido dejó de estar Pendiente: la pantalla avisa y regresa a la lista. */
    val unavailable: Boolean = false,
    val message: String? = null,
)
