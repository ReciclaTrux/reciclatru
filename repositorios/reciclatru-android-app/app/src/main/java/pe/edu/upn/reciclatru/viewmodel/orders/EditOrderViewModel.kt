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
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.RemoveOrderPhotoUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UpdateOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UploadOrderPhotosUseCase
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderItem
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * Edición de un pedido Pendiente sin aceptaciones (HU-07): ítems y fotos; la ubicación no
 * se edita. El guardado va por pasos (ítems, quitar fotos, subir fotos nuevas) y cada paso
 * hecho no se repite al reintentar, así no se pierden los cambios si algo falla a medias.
 */
@HiltViewModel
class EditOrderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getOrder: GetOrderUseCase,
    private val updateOrder: UpdateOrderUseCase,
    private val removePhoto: RemoveOrderPhotoUseCase,
    private val uploadPhotos: UploadOrderPhotosUseCase,
) : ViewModel() {

    private val orderId: String = savedStateHandle.get<String>(OrderIdArg).orEmpty()

    private var nextItemId = 0L
    private var initialItems: List<Pair<String, String>> = emptyList()
    private var removedPhotoIds: List<String> = emptyList()
    private var itemsSaved = false

    private val _uiState = MutableStateFlow(EditOrderUiState())
    val uiState: StateFlow<EditOrderUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    fun onAddItem() {
        val current = _uiState.value
        if (current.saving || current.items.size >= MaxItems) return
        updateItems { it + OrderItemInput(id = nextItemId++) }
    }

    fun onRemoveItem(id: Long) {
        if (_uiState.value.saving) return
        updateItems { items -> items.filterNot { it.id == id } }
    }

    fun onDescriptionChange(id: Long, value: String) {
        updateItems { items ->
            items.map {
                if (it.id == id) it.copy(description = value.take(MaxDescriptionLength)).withoutError() else it
            }
        }
    }

    fun onWeightChange(id: Long, value: String) {
        if (!WeightInputPattern.matches(value)) return
        updateItems { items ->
            items.map { if (it.id == id) it.copy(weight = value).withoutError() else it }
        }
    }

    fun onPhotoAdded(photo: String) {
        val current = _uiState.value
        if (current.saving || current.photoCount >= MaxPhotos) return
        _uiState.update { it.copy(newPhotos = it.newPhotos + photo) }
        refreshDirty()
    }

    /** [index] cuenta primero las fotos del pedido y después las nuevas, como las muestra el selector. */
    fun onPhotoRemoved(index: Int) {
        val current = _uiState.value
        if (current.saving) return
        if (index < current.keptPhotos.size) {
            val removed = current.keptPhotos[index]
            removedPhotoIds = removedPhotoIds + removed.id
            _uiState.update { it.copy(keptPhotos = it.keptPhotos.filterIndexed { position, _ -> position != index }) }
        } else {
            val newIndex = index - current.keptPhotos.size
            _uiState.update { it.copy(newPhotos = it.newPhotos.filterIndexed { position, _ -> position != newIndex }) }
        }
        refreshDirty()
    }

    /** Atrás o cerrar: con cambios pregunta si se descartan; sin cambios deja salir. */
    fun onBackRequested(): Boolean {
        val current = _uiState.value
        if (current.saving) return true
        if (!current.dirty) return false
        _uiState.update { it.copy(discardDialogVisible = true) }
        return true
    }

    fun onDiscardDismissed() {
        _uiState.update { it.copy(discardDialogVisible = false) }
    }

    fun onSave() {
        val current = _uiState.value
        if (current.saving || current.finished) return
        if (!current.dirty) {
            _uiState.update { it.copy(finished = true, finishMessage = null) }
            return
        }
        if (current.items.isEmpty()) {
            _uiState.update { it.copy(itemsError = NoItemsMessage, saveError = null) }
            return
        }
        val validated = current.items.map { item ->
            val descriptionInvalid = item.description.isBlank()
            val weightInvalid = !item.weight.toTenths().isValidWeight()
            item.copy(
                descriptionInvalid = descriptionInvalid,
                weightInvalid = weightInvalid,
                error = if (descriptionInvalid || weightInvalid) ItemInvalidMessage else null,
            )
        }
        if (validated.any { it.error != null }) {
            _uiState.update { it.copy(items = validated, saveError = null) }
            return
        }

        val orderItems = validated.map {
            OrderItem(description = it.description.trim(), weightKg = (it.weight.toTenths() ?: 0) / TenthsPerKg)
        }
        _uiState.update { it.copy(saving = true, saveError = null, itemsError = null) }
        viewModelScope.launch {
            try {
                if (!itemsSaved && itemsChanged(validated)) {
                    updateOrder(orderId, orderItems)
                }
                itemsSaved = true
                while (removedPhotoIds.isNotEmpty()) {
                    removePhoto(orderId, removedPhotoIds.first())
                    removedPhotoIds = removedPhotoIds.drop(1)
                }
                val newPhotos = _uiState.value.newPhotos
                if (newPhotos.isNotEmpty()) {
                    _uiState.update { it.copy(uploadingPhotos = true) }
                    uploadPhotos(orderId, newPhotos)
                }
                _uiState.update {
                    it.copy(
                        saving = false,
                        uploadingPhotos = false,
                        finished = true,
                        finishMessage = SavedMessage,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                val notEditable = (error as? NetworkException)?.code == NotEditableCode
                _uiState.update {
                    if (notEditable) {
                        it.copy(saving = false, uploadingPhotos = false, finished = true, finishMessage = NotEditableMessage)
                    } else {
                        it.copy(saving = false, uploadingPhotos = false, saveError = error.toSaveMessage())
                    }
                }
            }
        }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val order = getOrder(orderId)
                if (!order.canEdit) {
                    _uiState.update {
                        it.copy(loading = false, finished = true, finishMessage = NotEditableMessage)
                    }
                    return@launch
                }
                prefill(order)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
            }
        }
    }

    private fun prefill(order: Order) {
        val inputs = order.items.map {
            OrderItemInput(id = nextItemId++, description = it.description, weight = it.weightKg.toWeightText())
        }
        initialItems = inputs.map { it.description to it.weight }
        removedPhotoIds = emptyList()
        itemsSaved = false
        _uiState.update {
            it.copy(
                loading = false,
                items = inputs,
                totalWeightLabel = totalLabel(inputs),
                keptPhotos = order.photos,
                newPhotos = emptyList(),
                dirty = false,
            )
        }
    }

    private fun updateItems(transform: (List<OrderItemInput>) -> List<OrderItemInput>) {
        // Un cambio nuevo en los ítems vuelve a pedir guardarlos aunque un intento anterior los haya enviado.
        itemsSaved = false
        _uiState.update { state ->
            val items = transform(state.items)
            state.copy(
                items = items,
                totalWeightLabel = totalLabel(items),
                itemsError = null,
                saveError = null,
            )
        }
        refreshDirty()
    }

    private fun refreshDirty() {
        _uiState.update { state ->
            state.copy(
                dirty = itemsChanged(state.items) || removedPhotoIds.isNotEmpty() || state.newPhotos.isNotEmpty(),
            )
        }
    }

    private fun itemsChanged(items: List<OrderItemInput>): Boolean =
        items.map { it.description.trim() to it.weight.normalizedWeight() } !=
            initialItems.map { it.first.trim() to it.second.normalizedWeight() }

    private fun totalLabel(items: List<OrderItemInput>): String {
        val totalTenths = items.sumOf { it.weight.toTenths()?.takeIf { tenths -> tenths.isValidWeight() } ?: 0 }
        return if (totalTenths % 10 == 0) "${totalTenths / 10} kg" else "${totalTenths / 10}.${totalTenths % 10} kg"
    }

    private fun OrderItemInput.withoutError(): OrderItemInput =
        copy(error = null, descriptionInvalid = false, weightInvalid = false)

    private fun Double.toWeightText(): String {
        val tenths = Math.round(this * 10).toInt()
        return if (tenths % 10 == 0) "${tenths / 10}" else "${tenths / 10}.${tenths % 10}"
    }

    private fun String.normalizedWeight(): String = toTenths()?.toString() ?: this

    private fun String.toTenths(): Int? {
        val normalized = replace(',', '.')
        if (normalized.isBlank() || normalized == ".") return null
        val whole = normalized.substringBefore('.').ifEmpty { "0" }.toIntOrNull() ?: return null
        val decimal = normalized.substringAfter('.', "").ifEmpty { "0" }.toIntOrNull() ?: return null
        return whole * 10 + decimal
    }

    private fun Int?.isValidWeight(): Boolean = this != null && this in 1..MaxWeightTenths

    private fun Exception.toSaveMessage(): String = when ((this as? NetworkException)?.code) {
        InvalidItemsCode, InvalidWeightCode -> InvalidDataMessage
        else -> toUserMessage()
    }

    companion object {
        const val OrderIdArg = "orderId"
        private const val MaxItems = 10
        private const val MaxPhotos = 3
        private const val MaxDescriptionLength = 100
        private const val MaxWeightTenths = 5000
        private const val TenthsPerKg = 10.0
        private val WeightInputPattern = Regex("^\\d{0,3}([.,]\\d?)?$")
        private const val NotEditableCode = "ORDER_NOT_EDITABLE"
        private const val InvalidItemsCode = "INVALID_ITEMS"
        private const val InvalidWeightCode = "INVALID_WEIGHT"
        private const val NoItemsMessage = "Agrega al menos un ítem para guardar"
        private const val ItemInvalidMessage = "Escribe el material y un peso mayor a 0 y hasta 500 kg"
        private const val InvalidDataMessage = "Revisa los ítems e inténtalo de nuevo."
        private const val SavedMessage = "Cambios guardados"
        private const val NotEditableMessage = "Este pedido ya no se puede editar"
    }
}
