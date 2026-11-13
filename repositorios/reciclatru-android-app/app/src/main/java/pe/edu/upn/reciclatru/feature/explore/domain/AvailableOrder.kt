package pe.edu.upn.reciclatru.feature.explore.domain

import java.time.Instant

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class AvailableOrder(
    val id: String,
    val zone: String,
    val itemCount: Int,
    val totalWeightKg: Double,
    val distanceKm: Double?,
    val alreadyAccepted: Boolean,
    val createdAt: Instant?,
)

data class AvailableOrdersPage(
    val items: List<AvailableOrder>,
    val nextCursor: String?,
    val sortedByDistance: Boolean,
)
