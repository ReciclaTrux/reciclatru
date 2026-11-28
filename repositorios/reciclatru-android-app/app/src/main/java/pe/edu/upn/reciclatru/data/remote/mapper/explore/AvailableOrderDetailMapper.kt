package pe.edu.upn.reciclatru.data.remote.mapper.explore

import pe.edu.upn.reciclatru.data.remote.dto.orders.PickupDetailDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderOutputDto
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toAcceptanceStatus
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toInstantOrNull
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrderContact
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrderItem
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrderPhoto
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrderStatus
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.explore.PickupPoint
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus

fun ServicingOrderOutputDto.toAvailableOrderDetail(): AvailableOrderDetail? {
    val record = record ?: return null
    val id = record.id ?: crReferenceId ?: return null
    val status = record.status.toOrderStatus() ?: return null
    val items = record.items.orEmpty().mapNotNull { it.toOrderItem() }
    // `alreadyAccepted` sin estado se trata como aceptación pendiente de elegir.
    val acceptanceStatus = record.acceptanceStatus.toAcceptanceStatus()
        ?: AcceptanceStatus.Accepted.takeIf { record.alreadyAccepted == true }
    return AvailableOrderDetail(
        id = id,
        status = status,
        zone = record.zone.orEmpty(),
        items = items,
        totalWeightKg = record.totalWeightKg ?: items.sumOf { it.weightKg },
        distanceKm = record.approximateDistanceKm,
        createdAt = record.createdAt.toInstantOrNull(),
        acceptanceCount = record.acceptanceCount ?: 0,
        acceptanceStatus = acceptanceStatus,
        photos = record.photos.orEmpty().mapNotNull { it.toOrderPhoto() },
        neighbor = record.counterpart?.toOrderContact(),
        pickup = record.pickup?.toPickupPoint(),
    )
}

private fun PickupDetailDto.toPickupPoint(): PickupPoint? {
    val latitude = latitude ?: return null
    val longitude = longitude ?: return null
    return PickupPoint(
        address = address?.takeIf { it.isNotBlank() },
        reference = reference?.takeIf { it.isNotBlank() },
        latitude = latitude,
        longitude = longitude,
    )
}
