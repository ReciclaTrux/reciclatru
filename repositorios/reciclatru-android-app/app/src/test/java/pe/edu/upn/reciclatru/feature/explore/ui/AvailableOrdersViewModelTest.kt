package pe.edu.upn.reciclatru.feature.explore.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrder
import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrdersPage
import pe.edu.upn.reciclatru.feature.explore.domain.DeviceLocationProvider
import pe.edu.upn.reciclatru.feature.explore.domain.ExploreRepository
import pe.edu.upn.reciclatru.feature.explore.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.explore.domain.GetAvailableOrdersUseCase
import pe.edu.upn.reciclatru.feature.explore.domain.LocationPromptStore

@OptIn(ExperimentalCoroutinesApi::class)
class AvailableOrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trujillo = GeoPoint(-8.1116, -79.0288)

    @Test
    fun `la primera vez sin permiso explica para que se usa la ubicacion`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository()
            val viewModel = createViewModel(repository)

            viewModel.onStart(permissionGranted = false)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.showLocationPrompt)
            assertTrue(repository.calls.isEmpty())
        }

    @Test
    fun `con permiso envia la ubicacion y ordena por distancia`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository, location = trujillo)

        viewModel.onStart(permissionGranted = true)
        assertTrue(viewModel.uiState.value.loading)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertTrue(state.sortedByDistance)
        assertNull(state.locationNotice)
        assertEquals(listOf("d1", "d2"), state.orders.map { it.id })
        assertEquals(listOf(trujillo to null), repository.calls)
    }

    @Test
    fun `al aceptar la explicacion y dar el permiso carga con ubicacion`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository()
            val promptStore = FakePromptStore()
            val viewModel = createViewModel(repository, location = trujillo, promptStore = promptStore)
            viewModel.onStart(permissionGranted = false)

            viewModel.onPromptAccepted()
            viewModel.onPermissionResult(granted = true, permanentlyDenied = false)
            advanceUntilIdle()

            assertTrue(promptStore.shown)
            assertFalse(viewModel.uiState.value.showLocationPrompt)
            assertEquals(listOf(trujillo to null), repository.calls)
        }

    @Test
    fun `con ahora no carga por fecha y pide activar la ubicacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository, location = trujillo)
        viewModel.onStart(permissionGranted = false)

        viewModel.onPromptDismissed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.sortedByDistance)
        assertEquals(LocationNotice.PermissionDenied, state.locationNotice)
        assertEquals(listOf<Pair<GeoPoint?, String?>>(null to null), repository.calls)
    }

    @Test
    fun `si ya se explico antes no vuelve a mostrar la explicacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(), promptStore = FakePromptStore(shown = true))

        viewModel.onStart(permissionGranted = false)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.showLocationPrompt)
        assertTrue(viewModel.uiState.value.loaded)
    }

    @Test
    fun `con no volver a preguntar ofrece abrir los ajustes`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(), promptStore = FakePromptStore(shown = true))
        viewModel.onStart(permissionGranted = false)
        advanceUntilIdle()

        viewModel.onPermissionResult(granted = false, permanentlyDenied = true)
        advanceUntilIdle()

        assertEquals(LocationNotice.PermissionDeniedPermanently, viewModel.uiState.value.locationNotice)
    }

    @Test
    fun `con permiso pero sin ubicacion disponible ordena por fecha y avisa`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository()
            val viewModel = createViewModel(repository, location = null)

            viewModel.onStart(permissionGranted = true)
            advanceUntilIdle()

            assertEquals(LocationNotice.Unavailable, viewModel.uiState.value.locationNotice)
            assertFalse(viewModel.uiState.value.sortedByDistance)
        }

    @Test
    fun `al volver de ajustes con el permiso activo recarga con ubicacion`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository()
            val viewModel = createViewModel(
                repository,
                location = trujillo,
                promptStore = FakePromptStore(shown = true),
            )
            viewModel.onStart(permissionGranted = false)
            advanceUntilIdle()

            viewModel.onResume(permissionGranted = true)
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.locationNotice)
            assertEquals(trujillo, repository.calls.last().first)
        }

    @Test
    fun `sin pedidos queda la lista vacia y sin error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(orders = emptyList()), location = trujillo)

        viewModel.onStart(permissionGranted = true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.loaded)
        assertTrue(viewModel.uiState.value.orders.isEmpty())
        assertNull(viewModel.uiState.value.loadError)
    }

    @Test
    fun `si falla la carga muestra el error y reintentar vuelve a cargar`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository(error = noConnection())
            val viewModel = createViewModel(repository, location = trujillo)
            viewModel.onStart(permissionGranted = true)
            advanceUntilIdle()
            assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.loadError)

            repository.error = null
            viewModel.onRetry()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.loadError)
            assertEquals(2, viewModel.uiState.value.orders.size)
        }

    @Test
    fun `cargar mas agrega la pagina siguiente con la misma ubicacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(cursor = "cursor-2")
        val viewModel = createViewModel(repository, location = trujillo)
        viewModel.onStart(permissionGranted = true)
        advanceUntilIdle()

        viewModel.onLoadMore()
        viewModel.onLoadMore()
        advanceUntilIdle()

        assertEquals(listOf("d1", "d2", "d3"), viewModel.uiState.value.orders.map { it.id })
        assertNull(viewModel.uiState.value.nextCursor)
        assertEquals(listOf(trujillo to null, trujillo to "cursor-2"), repository.calls)
    }

    @Test
    fun `al actualizar desaparece el pedido que dejo de estar pendiente`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeExploreRepository()
            val viewModel = createViewModel(repository, location = trujillo)
            viewModel.onStart(permissionGranted = true)
            advanceUntilIdle()

            repository.orders = listOf(order("d2"))
            viewModel.onRefresh()
            advanceUntilIdle()

            assertEquals(listOf("d2"), viewModel.uiState.value.orders.map { it.id })
            assertFalse(viewModel.uiState.value.refreshing)
        }

    private fun createViewModel(
        repository: ExploreRepository,
        location: GeoPoint? = null,
        promptStore: LocationPromptStore = FakePromptStore(),
    ) = AvailableOrdersViewModel(
        getAvailableOrders = GetAvailableOrdersUseCase(repository),
        locationProvider = object : DeviceLocationProvider {
            override suspend fun currentLocation(): GeoPoint? = location
        },
        promptStore = promptStore,
    )

    private fun noConnection() = NetworkException(
        NetworkErrorKind.NoConnection,
        "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
    )

    private class FakePromptStore(var shown: Boolean = false) : LocationPromptStore {
        override fun wasShown(): Boolean = shown
        override fun markShown() {
            shown = true
        }
    }

    private class FakeExploreRepository(
        var orders: List<AvailableOrder> = listOf(order("d1"), order("d2")),
        private val cursor: String? = null,
        var error: Exception? = null,
    ) : ExploreRepository {
        val calls = mutableListOf<Pair<GeoPoint?, String?>>()

        override suspend fun getAvailableOrders(location: GeoPoint?, cursor: String?): AvailableOrdersPage {
            calls += location to cursor
            error?.let { throw it }
            return if (cursor == null) {
                AvailableOrdersPage(orders, this.cursor, sortedByDistance = location != null)
            } else {
                AvailableOrdersPage(listOf(order("d3")), null, sortedByDistance = location != null)
            }
        }
    }
}

private fun order(id: String) = AvailableOrder(
    id = id,
    zone = "Víctor Larco Herrera",
    itemCount = 2,
    totalWeightKg = 6.4,
    distanceKm = 0.8,
    alreadyAccepted = false,
    createdAt = null,
)
