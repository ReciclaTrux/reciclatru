package pe.edu.upn.reciclatru.feature.orders.data

import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrderSummary
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersPage
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
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
    )
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
    )
}

private fun String?.toInstantOrNull(): Instant? =
    this?.let { value -> runCatching { Instant.parse(value) }.getOrNull() }

private fun OrderItemDto.toOrderItem(): OrderItem? {
    val description = description ?: return null
    val weightKg = weightKg ?: return null
    return OrderItem(description = description, weightKg = weightKg)
}

private fun String?.toOrderStatus(): OrderStatus? = when (this) {
    "PENDING" -> OrderStatus.Pending
    "CONFIRMED" -> OrderStatus.Confirmed
    "COLLECTED" -> OrderStatus.Collected
    "CANCELLED" -> OrderStatus.Cancelled
    else -> null
}
