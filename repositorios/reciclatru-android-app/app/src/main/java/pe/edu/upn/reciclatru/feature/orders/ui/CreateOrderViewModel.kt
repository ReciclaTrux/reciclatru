package pe.edu.upn.reciclatru.feature.orders.ui

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
import pe.edu.upn.reciclatru.feature.orders.domain.CreateOrderUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.UploadOrderPhotosUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.GetProfileUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class CreateOrderViewModel @Inject constructor(
    private val createOrder: CreateOrderUseCase,
    private val getProfile: GetProfileUseCase,
    private val uploadPhotos: UploadOrderPhotosUseCase,
) : ViewModel() {

    private var nextItemId = 0L
    private var loadingProfile = false

    private val _uiState = MutableStateFlow(CreateOrderUiState(items = listOf(OrderItemInput(id = nextItemId++))))
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onResume() {
        if (_uiState.value.pickupAddress == null) loadProfile()
    }

    fun onAddItem() {
        val current = _uiState.value
        if (current.publishing || current.items.size >= MaxItems) return
        updateItems { it + OrderItemInput(id = nextItemId++) }
    }

    fun onRemoveItem(id: Long) {
        if (_uiState.value.publishing) return
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
        if (current.publishing || current.photos.size >= MaxPhotos) return
        _uiState.update { it.copy(photos = it.photos + photo) }
    }

    fun onPhotoRemoved(index: Int) {
        if (_uiState.value.publishing) return
        _uiState.update { it.copy(photos = it.photos.filterIndexed { position, _ -> position != index }) }
    }

    fun onProfileDialogDismissed() {
        _uiState.update { it.copy(profileIncomplete = false) }
    }

    fun onPublish() {
        val current = _uiState.value
        if (current.publishing || current.published) return

        if (current.items.isEmpty()) {
            _uiState.update { it.copy(itemsError = NoItemsMessage, publishError = null) }
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
            _uiState.update { it.copy(items = validated, publishError = null) }
            return
        }

        val orderItems = validated.map {
            OrderItem(description = it.description.trim(), weightKg = (it.weight.toTenths() ?: 0) / TenthsPerKg)
        }
        _uiState.update { it.copy(publishing = true, publishError = null, itemsError = null) }
        viewModelScope.launch {
            try {
                val order = createOrder(orderItems)
                val photos = _uiState.value.photos
                if (photos.isNotEmpty()) {
                    _uiState.update { it.copy(uploadingPhotos = true) }
                    uploadPhotos(order.id, photos)
                }
                _uiState.update {
                    it.copy(publishing = false, uploadingPhotos = false, published = true, publishedOrderId = order.id)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                if ((error as? NetworkException)?.code == ProfileIncompleteCode) {
                    _uiState.update { it.copy(publishing = false, profileIncomplete = true) }
                } else {
                    _uiState.update { it.copy(publishing = false, publishError = error.toPublishMessage()) }
                }
            }
        }
    }

    private fun loadProfile() {
        if (loadingProfile) return
        loadingProfile = true
        viewModelScope.launch {
            try {
                val profile = getProfile()
                val address = profile.pickupAddress
                _uiState.update {
                    it.copy(
                        pickupAddress = address?.let { pickup ->
                            listOf(pickup.address, pickup.district).filter(String::isNotBlank).joinToString(", ")
                        },
                        profileIncomplete = address == null,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            } finally {
                loadingProfile = false
            }
        }
    }

    private fun updateItems(transform: (List<OrderItemInput>) -> List<OrderItemInput>) {
        _uiState.update { state ->
            val items = transform(state.items)
            val totalTenths = items.sumOf { it.weight.toTenths()?.takeIf { tenths -> tenths.isValidWeight() } ?: 0 }
            state.copy(
                items = items,
                totalWeightLabel = totalTenths.toWeightLabel(),
                itemsError = null,
                publishError = null,
            )
        }
    }

    private fun OrderItemInput.withoutError(): OrderItemInput =
        copy(error = null, descriptionInvalid = false, weightInvalid = false)

    private fun String.toTenths(): Int? {
        val normalized = replace(',', '.')
        if (normalized.isBlank() || normalized == ".") return null
        val whole = normalized.substringBefore('.').ifEmpty { "0" }.toIntOrNull() ?: return null
        val decimal = normalized.substringAfter('.', "").ifEmpty { "0" }.toIntOrNull() ?: return null
        return whole * 10 + decimal
    }

    private fun Int?.isValidWeight(): Boolean = this != null && this in 1..MaxWeightTenths

    private fun Int.toWeightLabel(): String =
        if (this % 10 == 0) "${this / 10} kg" else "${this / 10}.${this % 10} kg"

    private fun Exception.toPublishMessage(): String = when ((this as? NetworkException)?.code) {
        InvalidItemsCode, InvalidWeightCode -> InvalidDataMessage
        else -> toUserMessage()
    }

    private companion object {
        const val MaxItems = 10
        const val MaxPhotos = 3
        const val MaxDescriptionLength = 100
        const val MaxWeightTenths = 5000
        const val TenthsPerKg = 10.0
        val WeightInputPattern = Regex("^\\d{0,3}([.,]\\d?)?$")
        const val ProfileIncompleteCode = "PROFILE_INCOMPLETE"
        const val InvalidItemsCode = "INVALID_ITEMS"
        const val InvalidWeightCode = "INVALID_WEIGHT"
        const val NoItemsMessage = "Agrega al menos un ítem para publicar"
        const val ItemInvalidMessage = "Escribe el material y un peso mayor a 0 y hasta 500 kg"
        const val InvalidDataMessage = "Revisa los ítems e inténtalo de nuevo."
    }
}
