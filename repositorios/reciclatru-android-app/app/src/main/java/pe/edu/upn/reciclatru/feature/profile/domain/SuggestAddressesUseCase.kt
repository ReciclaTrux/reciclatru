package pe.edu.upn.reciclatru.feature.profile.domain

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
