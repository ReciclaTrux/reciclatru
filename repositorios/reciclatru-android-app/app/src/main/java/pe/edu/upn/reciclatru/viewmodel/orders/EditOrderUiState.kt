package pe.edu.upn.reciclatru.viewmodel.orders

import pe.edu.upn.reciclatru.model.orders.OrderPhoto

data class EditOrderUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val items: List<OrderItemInput> = emptyList(),
    val totalWeightLabel: String = "0 kg",
    /** Fotos que ya tiene el pedido y no se han quitado. */
    val keptPhotos: List<OrderPhoto> = emptyList(),
    /** Fotos nuevas, todavía locales. */
    val newPhotos: List<String> = emptyList(),
    val dirty: Boolean = false,
    val itemsError: String? = null,
    val saveError: String? = null,
    val saving: Boolean = false,
    val uploadingPhotos: Boolean = false,
    val discardDialogVisible: Boolean = false,
    /** Terminó el guardado; la pantalla vuelve al detalle con [finishMessage]. */
    val finished: Boolean = false,
    val finishMessage: String? = null,
) {
    val photoCount: Int get() = keptPhotos.size + newPhotos.size
}
