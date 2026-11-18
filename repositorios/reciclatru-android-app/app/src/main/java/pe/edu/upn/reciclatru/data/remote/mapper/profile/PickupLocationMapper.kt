package pe.edu.upn.reciclatru.data.remote.mapper.profile

import pe.edu.upn.reciclatru.data.remote.dto.profile.PartyProfileOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.profile.PickupPointRequestDto
import pe.edu.upn.reciclatru.model.profile.GeoPoint
import pe.edu.upn.reciclatru.model.profile.PickupLocation

internal fun PickupLocation.toRequestDto(): PickupPointRequestDto = PickupPointRequestDto(
    latitude = point.latitude,
    longitude = point.longitude,
    address = address,
    district = district,
)

/**
 * Toma el punto de recojo del perfil que devolvió la API y, si un campo opcional vino
 * vacío, conserva el valor que se envió. La pantalla nunca muestra una dirección inventada.
 */
internal fun PartyProfileOutputDto.toPickupLocation(fallback: PickupLocation): PickupLocation {
    val pickupPoint = record?.pickupPoint
    val latitude = pickupPoint?.latitude
    val longitude = pickupPoint?.longitude
    return PickupLocation(
        id = fallback.id,
        point = if (latitude != null && longitude != null) {
            GeoPoint(latitude = latitude, longitude = longitude)
        } else {
            fallback.point
        },
        address = pickupPoint?.address?.takeIf { it.isNotBlank() } ?: fallback.address,
        district = pickupPoint?.district?.takeIf { it.isNotBlank() } ?: fallback.district,
        referenceNote = record?.reference ?: fallback.referenceNote,
    )
}
