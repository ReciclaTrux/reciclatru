package pe.edu.upn.reciclatru.feature.profile.domain

/** Punto geográfico en grados decimales. */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitud fuera de rango: $latitude" }
        require(longitude in -180.0..180.0) { "Longitud fuera de rango: $longitude" }
    }
}

/**
 * Centro de Trujillo: punto de partida del mapa cuando todavía no hay una ubicación
 * guardada. No es la ubicación del usuario ni reemplaza el GPS (que no se usa).
 */
val TrujilloCenter = GeoPoint(latitude = -8.1116, longitude = -79.0287)
