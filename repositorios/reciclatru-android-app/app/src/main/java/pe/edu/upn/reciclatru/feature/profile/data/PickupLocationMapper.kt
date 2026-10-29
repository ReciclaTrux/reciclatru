package pe.edu.upn.reciclatru.feature.profile.data

import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation

internal fun PickupLocation.toRequestDto(): PickupPointRequestDto = PickupPointRequestDto(
    latitude = point.latitude,
    longitude = point.longitude,
    address = address,
    district = district,
    referenceNote = referenceNote,
)

/**
 * Toma lo que devolvió la API y, si un campo opcional vino vacío, conserva el valor que
 * se envió. La pantalla nunca muestra una dirección inventada.
 */
internal fun PickupLocationDto.toDomain(fallback: PickupLocation): PickupLocation {
    val latitude = latitude
    val longitude = longitude
    return PickupLocation(
        id = id ?: fallback.id,
        point = if (latitude != null && longitude != null) {
            GeoPoint(latitude = latitude, longitude = longitude)
        } else {
            fallback.point
        },
        address = address?.takeIf { it.isNotBlank() } ?: fallback.address,
        district = district?.takeIf { it.isNotBlank() } ?: fallback.district,
        referenceNote = referenceNote ?: fallback.referenceNote,
    )
}
