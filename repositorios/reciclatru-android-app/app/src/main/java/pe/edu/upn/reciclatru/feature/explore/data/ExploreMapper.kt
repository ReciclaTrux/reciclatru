package pe.edu.upn.reciclatru.feature.explore.data

import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrder
import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrdersPage
import java.time.Instant

private const val PendingStatus = "PENDING"
private const val SortedByDistance = "DISTANCE"

fun ServicingOrderAvailabilityOutputDto.toAvailableOrdersPage(): AvailableOrdersPage = AvailableOrdersPage(
    items = items.orEmpty().mapNotNull { it.toAvailableOrder() },
    nextCursor = nextCursor,
    sortedByDistance = sortedBy == SortedByDistance,
)

private fun AvailableOrderDto.toAvailableOrder(): AvailableOrder? {
    val id = id ?: return null
    if (status != null && status != PendingStatus) return null
    return AvailableOrder(
        id = id,
        zone = zone.orEmpty(),
        itemCount = itemCount ?: 0,
        totalWeightKg = totalWeightKg ?: 0.0,
        distanceKm = approximateDistanceKm,
        alreadyAccepted = alreadyAccepted == true,
        createdAt = createdAt?.let { value -> runCatching { Instant.parse(value) }.getOrNull() },
    )
}
