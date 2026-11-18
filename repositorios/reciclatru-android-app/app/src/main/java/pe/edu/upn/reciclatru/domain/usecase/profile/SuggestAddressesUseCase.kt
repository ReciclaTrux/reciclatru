package pe.edu.upn.reciclatru.domain.usecase.profile

import pe.edu.upn.reciclatru.domain.repository.profile.AddressSuggestion
import pe.edu.upn.reciclatru.domain.repository.profile.GeocodingRepository
import javax.inject.Inject

/**
 * Devuelve hasta cuatro sugerencias para el autocomplete del buscador. El debounce y la
 * decisión de cuándo consultar viven en el ViewModel; acá solo se consulta al Geocoder.
 */
class SuggestAddressesUseCase @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
) {
    suspend operator fun invoke(query: String): List<AddressSuggestion> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()
        return geocodingRepository.suggest(trimmed)
    }
}
