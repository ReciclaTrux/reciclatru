package pe.edu.upn.reciclatru.data.remote.mapper.orders



import pe.edu.upn.reciclatru.data.remote.dto.orders.AcceptanceListOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.AcceptanceRecordDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ChooseRecyclerRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.CounterpartDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.OrderItemDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.OrderPhotoDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.OrderSummaryDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderInitiateRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderListOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderUpdateRequestDto
import pe.edu.upn.reciclatru.data.remote.toAbsoluteApiUrl
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderContact
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.model.orders.OrderSummary
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab
import java.time.Instant



fun List<OrderItem>.toInitiateRequest(): ServicingOrderInitiateRequestDto =
    ServicingOrderInitiateRequestDto(
        items = map { OrderItemDto(description = it.description, weightKg = it.weightKg) },
    )

fun ServicingOrderOutputDto.toOrder(): Order? {
    val record = record ?: return null
    val id = record.id ?: crReferenceId ?: return null
    val status = record.status.toOrderStatus() ?: return null
    val items = record.items.orEmpty().mapNotNull { it.toOrderItem() }
    return Order(
        id = id,
        status = status,
        items = items,
        totalWeightKg = record.totalWeightKg ?: items.sumOf { it.weightKg },
        acceptanceCount = record.acceptanceCount,
        chosenRecyclerName = record.chosenRecyclerName,
        createdAt = record.createdAt.toInstantOrNull(),
        counterpart = record.counterpart?.toOrderContact(),
        canCancel = record.availableActions?.contains(CancelAction)
            ?: (status == OrderStatus.Pending || status == OrderStatus.Confirmed),
        canChooseRecycler = record.availableActions?.contains(ChooseRecyclerAction)
            ?: (status == OrderStatus.Pending && (record.acceptanceCount ?: 0) > 0),
        canEdit = record.availableActions?.contains(EditAction)
            ?: (status == OrderStatus.Pending && (record.acceptanceCount ?: 0) == 0),
        canMarkCollected = record.availableActions?.contains(MarkCollectedAction)
            ?: (status == OrderStatus.Confirmed),
        photos = record.photos.orEmpty().mapNotNull { it.toOrderPhoto() },
    )
}

fun OrderPhotoDto.toOrderPhoto(): OrderPhoto? {
    val id = id ?: return null
    val url = url ?: return null
    return OrderPhoto(id = id, url = url.toAbsoluteApiUrl())
}

private const val CancelAction = "CANCEL"
private const val EditAction = "EDIT"
private const val MarkCollectedAction = "MARK_COLLECTED"

fun List<OrderItem>.toUpdateRequest(): ServicingOrderUpdateRequestDto =
    ServicingOrderUpdateRequestDto(
        items = map { OrderItemDto(description = it.description, weightKg = it.weightKg) },
    )
private const val ChooseRecyclerAction = "CHOOSE_RECYCLER"

fun String.toChooseRecyclerRequest(): ChooseRecyclerRequestDto = ChooseRecyclerRequestDto(acceptanceId = this)

fun AcceptanceListOutputDto.toAcceptances(): List<OrderAcceptance> =
    items.orEmpty().mapNotNull { it.toOrderAcceptance() }

private fun AcceptanceRecordDto.toOrderAcceptance(): OrderAcceptance? {
    val id = id ?: return null
    val fullName = fullName ?: return null
    val phone = phone ?: return null
    return OrderAcceptance(
        id = id,
        status = status.toAcceptanceStatus() ?: AcceptanceStatus.Accepted,
        fullName = fullName,
        phone = phone,
        acceptedAt = acceptedAt.toInstantOrNull(),
    )
}

internal fun String?.toAcceptanceStatus(): AcceptanceStatus? = when (this) {
    "ACCEPTED" -> AcceptanceStatus.Accepted
    "CHOSEN" -> AcceptanceStatus.Chosen
    "DISCARDED" -> AcceptanceStatus.Discarded
    else -> null
}

internal fun CounterpartDto.toOrderContact(): OrderContact? {
    val fullName = fullName ?: return null
    val phone = phone ?: return null
    return OrderContact(fullName = fullName, phone = phone)
}

fun OrdersTab.toQuery(): String = when (this) {
    OrdersTab.Active -> "ACTIVE"
    OrdersTab.History -> "HISTORY"
}

fun ServicingOrderListOutputDto.toOrdersPage(): OrdersPage = OrdersPage(
    items = items.orEmpty().mapNotNull { it.toOrderSummary() },
    nextCursor = nextCursor,
)

private fun OrderSummaryDto.toOrderSummary(): OrderSummary? {
    val id = id ?: return null
    val status = status.toOrderStatus() ?: return null
    return OrderSummary(
        id = id,
        status = status,
        itemCount = itemCount ?: 0,
        totalWeightKg = totalWeightKg ?: 0.0,
        acceptanceCount = acceptanceCount,
        chosenRecyclerName = chosenRecyclerName,
        createdAt = createdAt.toInstantOrNull(),
        thumbnailUrl = thumbnailUrl?.toAbsoluteApiUrl(),
    )
}

internal fun String?.toInstantOrNull(): Instant? =
    this?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }

internal fun OrderItemDto.toOrderItem(): OrderItem? {
    val description = description ?: return null
    val weightKg = weightKg ?: return null
    return OrderItem(description = description, weightKg = weightKg)
}

internal fun String?.toOrderStatus(): OrderStatus? = when (this) {
    "PENDING" -> OrderStatus.Pending
    "CONFIRMED" -> OrderStatus.Confirmed
    "COLLECTED" -> OrderStatus.Collected
    "CANCELLED" -> OrderStatus.Cancelled
    else -> null
}
