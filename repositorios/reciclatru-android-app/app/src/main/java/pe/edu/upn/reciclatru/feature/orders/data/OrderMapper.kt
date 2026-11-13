package pe.edu.upn.reciclatru.feature.orders.data

import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus

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
    )
}

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
