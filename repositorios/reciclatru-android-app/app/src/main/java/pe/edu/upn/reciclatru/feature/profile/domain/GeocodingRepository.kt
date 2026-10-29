package pe.edu.upn.reciclatru.feature.profile.domain

/**
 * Dirección sugerida por el geocoder para el autocomplete. El punto ya viene resuelto,
 * así elegir una sugerencia no necesita una segunda consulta.
 */
data class AddressSuggestion(
    val label: String,
    val point: GeoPoint,
)

/**
 * Búsqueda de direcciones y conversión de punto a calle. En Android la implementa el
 * Geocoder del sistema; en Flutter, el paquete `geocoding`. No pasa por la API.
 */
interface GeocodingRepository {
    /** `false` si el teléfono no trae el servicio; la pantalla oculta el buscador. */
    val isAvailable: Boolean

    /** Primer resultado para [query], o `null` si no hay coincidencias. */
    suspend fun search(query: String): GeoPoint?

    /** Hasta cuatro direcciones para el autocomplete, con su punto ya resuelto. */
    suspend fun suggest(query: String): List<AddressSuggestion>

    /** Calle y distrito del [point], o `null` si el servicio no los encuentra. */
    suspend fun describe(point: GeoPoint): PickupAddress?
}

/** El servicio de direcciones falló (no es lo mismo que "no hay resultados"). */
class GeocodingException(message: String, cause: Throwable? = null) : Exception(message, cause)
