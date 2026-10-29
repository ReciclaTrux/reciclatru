package pe.edu.upn.reciclatru.feature.profile.ui

import pe.edu.upn.reciclatru.feature.profile.domain.AddressSuggestion
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.feature.profile.domain.TrujilloCenter

/** Paso de HU-04: elegir el punto en el mapa, corregir la dirección o verla guardada. */
enum class LocationStep { Map, Address, Saved }

/**
 * Estado inmutable de la pantalla. Cada operación tiene su indicador (`searching`,
 * `resolvingAddress`, `saving`) y su error; la pantalla solo dibuja.
 */
data class LocationUiState(
    val step: LocationStep = LocationStep.Map,
    val center: GeoPoint = TrujilloCenter,
    val address: String = "",
    val district: String = "",
    val geocodingAvailable: Boolean = true,
    val searching: Boolean = false,
    val suggestions: List<AddressSuggestion> = emptyList(),
    val resolvingAddress: Boolean = false,
    val saving: Boolean = false,
    val searchError: String? = null,
    val addressError: String? = null,
    val districtError: String? = null,
    val saveError: String? = null,
    val savedLocation: PickupLocation? = null,
)
