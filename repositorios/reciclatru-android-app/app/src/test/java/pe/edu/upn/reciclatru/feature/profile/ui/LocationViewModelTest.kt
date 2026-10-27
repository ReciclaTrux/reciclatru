package pe.edu.upn.reciclatru.feature.profile.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.edu.upn.reciclatru.MainDispatcherRule
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.feature.profile.domain.AddressSuggestion
import pe.edu.upn.reciclatru.feature.profile.domain.DescribePointUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.GeocodingRepository
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.feature.profile.domain.ProfileRepository
import pe.edu.upn.reciclatru.feature.profile.domain.SavePickupLocationUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.SearchAddressUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.SuggestAddressesUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.TrujilloCenter

/**
 * Ejemplo de test de ViewModel de una HU: Geocoder y API falsos, sin Android ni red.
 * La pantalla no se prueba acá; esto valida la lógica y los estados que dibuja.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocationViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `inicia en el mapa y en el centro de Trujillo`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value

        assertEquals(LocationStep.Map, state.step)
        assertEquals(TrujilloCenter, state.center)
        assertTrue(state.geocodingAvailable)
        assertNull(state.searchError)
    }

    @Test
    fun `buscar una direccion mueve el centro del mapa`() = runTest(mainDispatcherRule.testDispatcher) {
        val found = GeoPoint(latitude = -8.1234, longitude = -79.0456)
        val geocoder = FakeGeocodingRepository(searchResult = found)
        val viewModel = createViewModel(geocoder = geocoder)

        viewModel.onSearch("Av. Larco 1250")
        advanceUntilIdle()

        assertEquals(found, viewModel.uiState.value.center)
        assertNull(viewModel.uiState.value.searchError)
    }

    @Test
    fun `buscar sin resultados muestra el error y no mueve el mapa`() = runTest(mainDispatcherRule.testDispatcher) {
        val geocoder = FakeGeocodingRepository(searchResult = null)
        val viewModel = createViewModel(geocoder = geocoder)

        viewModel.onSearch("Calle que no existe")
        advanceUntilIdle()

        assertEquals(TrujilloCenter, viewModel.uiState.value.center)
        assertEquals(
            "No encontramos esa dirección. Mueve el mapa a mano.",
            viewModel.uiState.value.searchError,
        )
    }

    @Test
    fun `confirmar el pin pasa a la direccion y la prellena`() = runTest(mainDispatcherRule.testDispatcher) {
        val described = PickupAddress(address = "Av. Larco 1250", district = "Víctor Larco Herrera")
        val geocoder = FakeGeocodingRepository(describeResult = described)
        val viewModel = createViewModel(geocoder = geocoder)

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocationStep.Address, state.step)
        assertEquals("Av. Larco 1250", state.address)
        assertEquals("Víctor Larco Herrera", state.district)
        assertFalse(state.resolvingAddress)
        assertNull(state.addressError)
    }

    @Test
    fun `si el geocoder no encuentra la calle deja los campos vacios y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val geocoder = FakeGeocodingRepository(describeResult = null)
        val viewModel = createViewModel(geocoder = geocoder)

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocationStep.Address, state.step)
        assertEquals("", state.address)
        assertEquals("", state.district)
        assertEquals("No pudimos leer la dirección. Escríbela tú.", state.addressError)
    }

    @Test
    fun `guardar con la direccion vacia no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val profile = FakeProfileRepository()
        val viewModel = createViewModel(profile = profile)

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()
        viewModel.onAddressChange("")
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals("Escribe tu dirección.", viewModel.uiState.value.addressError)
        assertNull(profile.saved)
        assertEquals(LocationStep.Address, viewModel.uiState.value.step)
    }

    @Test
    fun `guardar la ubicacion muestra el paso de guardada`() = runTest(mainDispatcherRule.testDispatcher) {
        val profile = FakeProfileRepository()
        val viewModel = createViewModel(profile = profile)

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()
        viewModel.onAddressChange("Av. Larco 1250")
        viewModel.onDistrictChange("Víctor Larco Herrera")
        viewModel.onSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocationStep.Saved, state.step)
        assertEquals("location-1", state.savedLocation?.id)
        assertEquals("Av. Larco 1250", profile.saved?.address)
        assertEquals(-79.0287, profile.saved?.point?.longitude ?: 0.0, 0.0001)
    }

    @Test
    fun `si la API falla se queda en la direccion con el mensaje`() = runTest(mainDispatcherRule.testDispatcher) {
        val profile = FakeProfileRepository(
            failure = NetworkException(
                kind = NetworkErrorKind.Server,
                message = "Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento.",
            ),
        )
        val viewModel = createViewModel(profile = profile)

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()
        viewModel.onAddressChange("Av. Larco 1250")
        viewModel.onDistrictChange("Víctor Larco Herrera")
        viewModel.onSave()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LocationStep.Address, state.step)
        assertFalse(state.saving)
        assertEquals(
            "Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento.",
            state.saveError,
        )
    }

    @Test
    fun `volver al mapa conserva el punto elegido`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onMapConfirmed(latitude = -8.1116, longitude = -79.0287)
        advanceUntilIdle()
        viewModel.onBackToMap()

        assertEquals(LocationStep.Map, viewModel.uiState.value.step)
        assertEquals(-8.1116, viewModel.uiState.value.center.latitude, 0.0)
    }

    @Test
    fun `escribir espera el debounce antes de pedir sugerencias`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val suggestion = sampleSuggestion()
            val geocoder = FakeGeocodingRepository(suggestResult = listOf(suggestion))
            val viewModel = createViewModel(geocoder = geocoder)

            viewModel.onSearchQueryChanged("Av. Larco")
            assertTrue(viewModel.uiState.value.searching)
            advanceTimeBy(299)
            assertTrue(geocoder.suggestQueries.isEmpty())

            advanceUntilIdle()
            assertEquals(listOf("Av. Larco"), geocoder.suggestQueries)
            assertEquals(listOf(suggestion), viewModel.uiState.value.suggestions)
            assertFalse(viewModel.uiState.value.searching)
        }

    @Test
    fun `escribir rapido cancela la consulta anterior`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val geocoder = FakeGeocodingRepository(suggestResult = listOf(sampleSuggestion()))
            val viewModel = createViewModel(geocoder = geocoder)

            viewModel.onSearchQueryChanged("Lar")
            advanceTimeBy(100)
            viewModel.onSearchQueryChanged("Larco")
            advanceUntilIdle()

            assertEquals(listOf("Larco"), geocoder.suggestQueries)
        }

    @Test
    fun `elegir una sugerencia mueve el centro y limpia la lista`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val suggestion = sampleSuggestion()
            val geocoder = FakeGeocodingRepository(suggestResult = listOf(suggestion))
            val viewModel = createViewModel(geocoder = geocoder)

            viewModel.onSearchQueryChanged("Larco")
            advanceUntilIdle()
            viewModel.onSuggestionSelected(suggestion.label)

            val state = viewModel.uiState.value
            assertEquals(suggestion.point, state.center)
            assertTrue(state.suggestions.isEmpty())
            assertFalse(state.searching)
            assertNull(state.searchError)
        }

    @Test
    fun `sin sugerencias muestra el error del buscador`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val geocoder = FakeGeocodingRepository(suggestResult = emptyList())
            val viewModel = createViewModel(geocoder = geocoder)

            viewModel.onSearchQueryChanged("Calle que no existe")
            advanceUntilIdle()

            assertEquals(
                "No encontramos esa dirección. Mueve el mapa a mano.",
                viewModel.uiState.value.searchError,
            )
            assertFalse(viewModel.uiState.value.searching)
        }

    @Test
    fun `borrar el texto limpia sugerencias y error`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val geocoder = FakeGeocodingRepository(suggestResult = listOf(sampleSuggestion()))
            val viewModel = createViewModel(geocoder = geocoder)

            viewModel.onSearchQueryChanged("Larco")
            advanceUntilIdle()
            viewModel.onSearchQueryChanged("")

            val state = viewModel.uiState.value
            assertTrue(state.suggestions.isEmpty())
            assertFalse(state.searching)
            assertNull(state.searchError)
        }

    private fun sampleSuggestion() = AddressSuggestion(
        label = "Av. Larco 1250, Víctor Larco Herrera",
        point = GeoPoint(latitude = -8.1122, longitude = -79.0274),
    )

    private fun createViewModel(
        geocoder: FakeGeocodingRepository = FakeGeocodingRepository(),
        profile: FakeProfileRepository = FakeProfileRepository(),
    ): LocationViewModel = LocationViewModel(
        searchAddress = SearchAddressUseCase(geocoder),
        suggestAddresses = SuggestAddressesUseCase(geocoder),
        describePoint = DescribePointUseCase(geocoder),
        savePickupLocation = SavePickupLocationUseCase(profile),
    )
}

private class FakeGeocodingRepository(
    override var isAvailable: Boolean = true,
    var searchResult: GeoPoint? = null,
    var suggestResult: List<AddressSuggestion> = emptyList(),
    var describeResult: PickupAddress? = PickupAddress("Av. Larco 1250", "Víctor Larco Herrera"),
    var failure: Exception? = null,
) : GeocodingRepository {

    val suggestQueries = mutableListOf<String>()

    override suspend fun search(query: String): GeoPoint? {
        failure?.let { throw it }
        return searchResult
    }

    override suspend fun suggest(query: String): List<AddressSuggestion> {
        failure?.let { throw it }
        suggestQueries += query
        return suggestResult
    }

    override suspend fun describe(point: GeoPoint): PickupAddress? {
        failure?.let { throw it }
        return describeResult
    }
}

private class FakeProfileRepository(
    private val failure: Exception? = null,
) : ProfileRepository {

    var saved: PickupLocation? = null

    override suspend fun savePickupLocation(location: PickupLocation): PickupLocation {
        failure?.let { throw it }
        return location.copy(id = "location-1").also { saved = it }
    }
}
