package pe.edu.upn.reciclatru.feature.profile.data

import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.feature.profile.domain.AddressSuggestion
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.GeocodingException
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Geocoder nativo de Android: no usa API key, no cuesta y no necesita permiso de
 * ubicación. Las llamadas bloquean, así que siempre corren en el dispatcher de IO.
 *
 * En algunos emuladores sin servicios de Google `Geocoder.isPresent()` es `false`; en ese
 * caso la pantalla oculta el buscador y deja mover el mapa a mano.
 */
@Singleton
class AndroidGeocoderDataSource @Inject constructor(
    private val geocoder: Geocoder,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    val isPresent: Boolean
        get() = Geocoder.isPresent()

    @Suppress("DEPRECATION")
    suspend fun search(query: String): GeoPoint? = withContext(ioDispatcher) {
        val results = try {
            geocoder.getFromLocationName(query.withTrujilloBias(), MaxResults)
        } catch (error: IOException) {
            throw GeocodingException("No pudimos buscar esa dirección.", error)
        }
        results?.firstOrNull()?.toGeoPoint()
    }

    /** Hasta cuatro coincidencias para el autocomplete, con su punto ya resuelto. */
    @Suppress("DEPRECATION")
    suspend fun suggest(query: String): List<AddressSuggestion> = withContext(ioDispatcher) {
        val results = try {
            geocoder.getFromLocationName(query.withTrujilloBias(), MaxSuggestions)
        } catch (error: IOException) {
            throw GeocodingException("No pudimos buscar esa dirección.", error)
        }
        results.orEmpty()
            .mapNotNull { address ->
                val parts = address.toLabelParts() ?: return@mapNotNull null
                AddressSuggestion(
                    label = "${parts.address}, ${parts.district}",
                    point = address.toGeoPoint(),
                )
            }
            .distinctBy { suggestion -> suggestion.label }
            .take(MaxSuggestions)
    }

    @Suppress("DEPRECATION")
    suspend fun describe(point: GeoPoint): PickupAddress? = withContext(ioDispatcher) {
        val results = try {
            geocoder.getFromLocation(point.latitude, point.longitude, MaxResults)
        } catch (error: IOException) {
            throw GeocodingException("No pudimos leer la dirección del pin.", error)
        }
        results?.firstOrNull()?.toLabelParts()?.let { parts ->
            PickupAddress(address = parts.address, district = parts.district)
        }
    }

    private fun Address.toGeoPoint(): GeoPoint =
        GeoPoint(latitude = latitude, longitude = longitude)

    /**
     * La calle con número es más corta que la dirección completa (que suele incluir
     * país); si no hay calle, se usa la línea completa como respaldo.
     */
    private fun Address.toLabelParts(): LabelParts? {
        val addressLine = getAddressLine(0)?.takeIf { it.isNotBlank() }
        val street = listOfNotNull(thoroughfare, subThoroughfare)
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
        val address = street ?: addressLine ?: return null
        val district = subLocality?.takeIf { it.isNotBlank() }
            ?: locality?.takeIf { it.isNotBlank() }
            ?: adminArea?.takeIf { it.isNotBlank() }
            ?: return null
        return LabelParts(address = address, district = district)
    }

    private data class LabelParts(val address: String, val district: String)

    /**
     * ReciclaTru atiende solo Trujillo. Si la persona no escribe la ciudad, se agrega para
     * que el Geocoder no devuelva calles con el mismo nombre en otros países.
     */
    private fun String.withTrujilloBias(): String =
        if (contains("trujillo", ignoreCase = true)) this else "$this, Trujillo, Perú"

    private companion object {
        /** Para confirmar el pin solo hace falta una coincidencia. */
        const val MaxResults = 1

        /** El sistema muestra cuatro sugerencias como máximo. */
        const val MaxSuggestions = 4
    }
}
