package pe.edu.upn.reciclatru.domain.usecase.profile

import pe.edu.upn.reciclatru.domain.repository.profile.GeocodingRepository
import pe.edu.upn.reciclatru.model.profile.GeoPoint
import javax.inject.Inject

/**
 * Busca una dirección y devuelve su punto. Un texto vacío no se consulta; si no hay
 * coincidencias devuelve `null` para que la pantalla explique y deje mover el mapa.
 */
class SearchAddressUseCase @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
) {
    val isAvailable: Boolean
        get() = geocodingRepository.isAvailable

    suspend operator fun invoke(query: String): GeoPoint? {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return null
        return geocodingRepository.search(trimmed)
    }
}
