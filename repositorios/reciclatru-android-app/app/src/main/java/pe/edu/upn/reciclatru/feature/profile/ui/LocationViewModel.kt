package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.profile.domain.AddressSuggestion
import pe.edu.upn.reciclatru.feature.profile.domain.DescribePointUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.feature.profile.domain.SavePickupLocationUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.SearchAddressUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.SuggestAddressesUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

/**
 * HU-04: elegir el punto de recojo en el mapa, completar la dirección y guardarla.
 *
 * No usa GPS ni permisos de ubicación. La búsqueda y la lectura de la calle las hace el
 * Geocoder del sistema; guardar lo valida la API.
 */
@HiltViewModel
class LocationViewModel @Inject constructor(
    private val searchAddress: SearchAddressUseCase,
    private val suggestAddresses: SuggestAddressesUseCase,
    private val describePoint: DescribePointUseCase,
    private val savePickupLocation: SavePickupLocationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LocationUiState(geocodingAvailable = searchAddress.isAvailable),
    )
    val uiState: StateFlow<LocationUiState> = _uiState.asStateFlow()

    private var suggestJob: Job? = null
    private var suggestRequestId = 0

    /**
     * Autocomplete con debounce de 300 ms. Cada tecla cancela la consulta anterior; el
     * Geocoder corre en la feature, nunca en el componente.
     */
    fun onSearchQueryChanged(query: String) {
        suggestJob?.cancel()
        val requestId = ++suggestRequestId

        if (query.isBlank()) {
            _uiState.update {
                it.copy(searching = false, suggestions = emptyList(), searchError = null)
            }
            return
        }

        _uiState.update {
            it.copy(searching = true, suggestions = emptyList(), searchError = null)
        }
        suggestJob = viewModelScope.launch {
            delay(SearchDebounceMillis)
            try {
                val found = suggestAddresses(query)
                if (requestId != suggestRequestId) return@launch
                _uiState.update {
                    it.copy(
                        searching = false,
                        suggestions = found,
                        searchError = if (found.isEmpty()) AddressNotFoundMessage else null,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                if (requestId != suggestRequestId) return@launch
                _uiState.update {
                    it.copy(
                        searching = false,
                        suggestions = emptyList(),
                        searchError = SearchServiceErrorMessage,
                    )
                }
            }
        }
    }

    /** Elige una sugerencia ya resuelta y mueve la cámara a su punto. */
    fun onSuggestionSelected(label: String) {
        suggestJob?.cancel()
        suggestRequestId++

        val known = _uiState.value.suggestions.firstOrNull { it.label == label }
        if (known != null) {
            _uiState.update {
                it.copy(
                    center = known.point,
                    searching = false,
                    suggestions = emptyList(),
                    searchError = null,
                )
            }
            return
        }

        // Respaldo: la lista cambió entre el toque y la respuesta; se resuelve por nombre.
        viewModelScope.launch {
            _uiState.update {
                it.copy(searching = true, suggestions = emptyList(), searchError = null)
            }
            try {
                val point = searchAddress(label)
                _uiState.update {
                    if (point == null) {
                        it.copy(searching = false, searchError = AddressNotFoundMessage)
                    } else {
                        it.copy(searching = false, center = point, searchError = null)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(searching = false, searchError = SearchServiceErrorMessage)
                }
            }
        }
    }

    /** Mueve el mapa al primer resultado; si no hay, deja el error bajo el buscador. */
    fun onSearch(query: String) {
        suggestJob?.cancel()
        suggestRequestId++

        if (query.isBlank()) {
            _uiState.update {
                it.copy(searching = false, suggestions = emptyList(), searchError = null)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(searching = true, suggestions = emptyList(), searchError = null)
            }
            try {
                val point = searchAddress(query)
                _uiState.update { state ->
                    if (point == null) {
                        state.copy(searching = false, searchError = AddressNotFoundMessage)
                    } else {
                        state.copy(searching = false, center = point, searchError = null)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(searching = false, searchError = SearchServiceErrorMessage)
                }
            }
        }
    }

    /** El pin quedó en [latitude]/[longitude]: pasa a completar la dirección. */
    fun onMapConfirmed(latitude: Double, longitude: Double) {
        suggestJob?.cancel()
        suggestRequestId++

        val point = GeoPoint(latitude = latitude, longitude = longitude)
        _uiState.update {
            it.copy(
                step = LocationStep.Address,
                center = point,
                address = "",
                district = "",
                resolvingAddress = true,
                searching = false,
                suggestions = emptyList(),
                searchError = null,
                addressError = null,
                districtError = null,
                saveError = null,
            )
        }

        viewModelScope.launch {
            try {
                val described = describePoint(point)
                _uiState.update { state ->
                    // Si la persona volvió al mapa, esta respuesta ya no sirve.
                    if (state.step != LocationStep.Address || state.center != point) {
                        state
                    } else if (described == null) {
                        state.copy(
                            resolvingAddress = false,
                            addressError = AddressUnknownMessage,
                        )
                    } else {
                        state.copy(
                            resolvingAddress = false,
                            address = described.address,
                            district = described.district,
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state ->
                    if (state.step != LocationStep.Address || state.center != point) {
                        state
                    } else {
                        state.copy(
                            resolvingAddress = false,
                            addressError = AddressUnknownMessage,
                        )
                    }
                }
            }
        }
    }

    fun onAddressChange(value: String) {
        _uiState.update { it.copy(address = value, addressError = null, saveError = null) }
    }

    fun onDistrictChange(value: String) {
        _uiState.update { it.copy(district = value, districtError = null, saveError = null) }
    }

    /** Valida lo mínimo para no enviar vacíos y guarda en la API. */
    fun onSave() {
        val state = _uiState.value
        if (state.saving) return

        val address = state.address.trim()
        val district = state.district.trim()
        val addressError = if (address.isEmpty()) "Escribe tu dirección." else null
        val districtError = if (district.isEmpty()) "Escribe tu distrito." else null
        if (addressError != null || districtError != null) {
            _uiState.update {
                it.copy(addressError = addressError, districtError = districtError)
            }
            return
        }

        _uiState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            try {
                val saved = savePickupLocation(
                    PickupLocation(
                        point = state.center,
                        address = address,
                        district = district,
                    ),
                )
                _uiState.update {
                    it.copy(
                        step = LocationStep.Saved,
                        saving = false,
                        saveError = null,
                        savedLocation = saved,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        saving = false,
                        saveError = error.toUserMessage(
                            fallback = "No pudimos guardar tu ubicación. Inténtalo de nuevo.",
                        ),
                    )
                }
            }
        }
    }

    /** Vuelve al mapa sin perder el punto; los textos se recalculan al confirmar. */
    fun onBackToMap() {
        _uiState.update {
            it.copy(
                step = LocationStep.Map,
                resolvingAddress = false,
                addressError = null,
                districtError = null,
                saveError = null,
            )
        }
    }

    private companion object {
        /** Mismo debounce que Flutter: 300 ms desde la última tecla. */
        const val SearchDebounceMillis = 300L
        const val AddressNotFoundMessage = "No encontramos esa dirección. Mueve el mapa a mano."
        const val AddressUnknownMessage = "No pudimos leer la dirección. Escríbela tú."
        const val SearchServiceErrorMessage = "No pudimos buscar la dirección. Mueve el mapa a mano."
    }
}
