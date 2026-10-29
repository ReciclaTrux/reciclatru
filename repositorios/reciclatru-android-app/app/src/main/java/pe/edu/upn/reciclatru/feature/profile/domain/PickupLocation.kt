package pe.edu.upn.reciclatru.feature.profile.domain

/** Dirección aproximada que el geocoder devuelve para un punto del mapa. */
data class PickupAddress(
    val address: String,
    val district: String,
)

/**
 * Punto de recojo del vecino. `id` viene de la API; es `null` mientras no se haya
 * guardado. La referencia es opcional y viaja con la ubicación (RN18).
 */
data class PickupLocation(
    val point: GeoPoint,
    val address: String,
    val district: String,
    val referenceNote: String? = null,
    val id: String? = null,
)
