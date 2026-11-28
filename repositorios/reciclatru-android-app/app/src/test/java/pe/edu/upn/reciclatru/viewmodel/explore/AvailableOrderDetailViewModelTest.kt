package pe.edu.upn.reciclatru.viewmodel.explore

import androidx.lifecycle.SavedStateHandle
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
import pe.edu.upn.reciclatru.data.remote.NetworkErrorKind
import pe.edu.upn.reciclatru.data.remote.NetworkException
import pe.edu.upn.reciclatru.domain.repository.explore.DeviceLocationProvider
import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.domain.usecase.explore.AcceptOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.explore.GetAvailableOrderDetailUseCase
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.explore.AvailableOrdersPage
import pe.edu.upn.reciclatru.model.explore.GeoPoint
import pe.edu.upn.reciclatru.model.explore.PickupPoint
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderContact
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderStatus

@OptIn(ExperimentalCoroutinesApi::class)
class AvailableOrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val trujillo = GeoPoint(-8.1116, -79.0288)

    @Test
    fun `al abrir carga el detalle con la ubicacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository, location = trujillo)

        assertTrue(viewModel.uiState.value.loading)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals("o1", viewModel.uiState.value.order?.id)
        assertEquals(listOf<GeoPoint?>(trujillo), repository.detailLocations)
    }

    @Test
    fun `sin ubicacion igual carga el detalle`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository, location = null)

        advanceUntilIdle()

        assertEquals("o1", viewModel.uiState.value.order?.id)
        assertEquals(listOf<GeoPoint?>(null), repository.detailLocations)
    }

    @Test
    fun `si el pedido ya no esta disponible al abrir avisa y regresa`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(detailError = notAvailable()))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.unavailable)
        assertNull(viewModel.uiState.value.loadError)
    }

    @Test
    fun `si falla la carga muestra el error y permite reintentar`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(detailError = noConnection())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(NoConnectionMessage, viewModel.uiState.value.loadError)
        assertNull(viewModel.uiState.value.order)

        repository.detailError = null
        viewModel.onRetry()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.loadError)
        assertEquals("o1", viewModel.uiState.value.order?.id)
    }

    @Test
    fun `aceptar pide confirmacion antes de llamar a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onAcceptClick()

        assertTrue(viewModel.uiState.value.confirmDialogVisible)
        assertEquals(0, repository.acceptCalls)

        viewModel.onAcceptDismissed()
        assertFalse(viewModel.uiState.value.confirmDialogVisible)
        assertEquals(0, repository.acceptCalls)
    }

    @Test
    fun `confirmar acepta el pedido y muestra el contacto del vecino`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertEquals(1, repository.acceptCalls)
        val order = viewModel.uiState.value.order
        assertTrue(order?.alreadyAccepted == true)
        assertEquals("Ana Torres", order?.neighbor?.fullName)
        assertFalse(viewModel.uiState.value.confirmDialogVisible)
        assertFalse(viewModel.uiState.value.accepting)
    }

    @Test
    fun `un doble toque en Aceptar registra una sola solicitud`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertEquals(1, repository.acceptCalls)
    }

    @Test
    fun `un pedido ya aceptado no vuelve a pedir confirmacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(accepted = true))
        advanceUntilIdle()

        viewModel.onAcceptClick()

        assertFalse(viewModel.uiState.value.confirmDialogVisible)
    }

    @Test
    fun `si el pedido dejo de estar pendiente al aceptar avisa y regresa`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(acceptError = notAvailable()))
        advanceUntilIdle()

        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.unavailable)
        assertFalse(viewModel.uiState.value.accepting)
    }

    @Test
    fun `si falta el perfil lleva a completarlo`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeExploreRepository(acceptError = profileIncomplete()))
        advanceUntilIdle()

        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileIncomplete)
        assertFalse(viewModel.uiState.value.order?.alreadyAccepted == true)

        viewModel.onProfileDialogDismissed()
        assertFalse(viewModel.uiState.value.profileIncomplete)
    }

    @Test
    fun `si falla la aceptacion sin conexion avisa y permite reintentar`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(acceptError = noConnection())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertEquals(NoConnectionMessage, viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.order?.alreadyAccepted == true)

        repository.acceptError = null
        viewModel.onAcceptClick()
        viewModel.onAcceptConfirmed()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.order?.alreadyAccepted == true)
    }

    @Test
    fun `deslizar hacia abajo muestra el estado actual del pedido`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(accepted = true)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val cancelled = viewModel.uiState.value.order!!.copy(status = OrderStatus.Cancelled, neighbor = null)

        repository.nextDetail = cancelled
        viewModel.onRefresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.refreshing)
        assertEquals(OrderStatus.Cancelled, viewModel.uiState.value.order?.status)
        assertNull(viewModel.uiState.value.order?.neighbor)
    }

    @Test
    fun `si falla al actualizar mantiene el pedido y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(accepted = true)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.detailError = noConnection()
        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals("o1", viewModel.uiState.value.order?.id)
        assertNull(viewModel.uiState.value.loadError)
        assertEquals(NoConnectionMessage, viewModel.uiState.value.message)
    }

    @Test
    fun `al volver a la pantalla se actualiza en silencio`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeExploreRepository(accepted = true)
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val pickup = PickupPoint("Av. Larco 1250", null, -8.13, -79.02)

        repository.nextDetail = viewModel.uiState.value.order!!.copy(
            status = OrderStatus.Confirmed,
            acceptanceStatus = AcceptanceStatus.Chosen,
            pickup = pickup,
        )
        viewModel.onResume()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals(pickup, viewModel.uiState.value.order?.pickup)
        assertEquals(2, repository.detailLocations.size)
    }

    private fun createViewModel(
        repository: ExploreRepository,
        location: GeoPoint? = null,
    ) = AvailableOrderDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(AvailableOrderDetailViewModel.OrderIdArg to "o1")),
        getOrderDetail = GetAvailableOrderDetailUseCase(repository),
        acceptOrder = AcceptOrderUseCase(repository),
        locationProvider = object : DeviceLocationProvider {
            override suspend fun currentLocation(): GeoPoint? = location
        },
    )

    private fun noConnection() = NetworkException(NetworkErrorKind.NoConnection, NoConnectionMessage)

    private fun notAvailable() = NetworkException(
        NetworkErrorKind.Conflict,
        "El pedido ya no está disponible.",
        statusCode = 409,
        code = "ORDER_NOT_AVAILABLE",
    )

    private fun profileIncomplete() = NetworkException(
        NetworkErrorKind.Unknown,
        "Completa tu perfil.",
        statusCode = 422,
        code = "PROFILE_INCOMPLETE",
    )

    private class FakeExploreRepository(
        var detailError: Exception? = null,
        var acceptError: Exception? = null,
        private val accepted: Boolean = false,
    ) : ExploreRepository {
        var nextDetail: AvailableOrderDetail? = null
        val detailLocations = mutableListOf<GeoPoint?>()
        var acceptCalls = 0

        override suspend fun getAvailableOrders(location: GeoPoint?, cursor: String?): AvailableOrdersPage =
            AvailableOrdersPage(items = emptyList(), nextCursor = null, sortedByDistance = false)

        override suspend fun getOrderDetail(orderId: String, location: GeoPoint?): AvailableOrderDetail {
            detailLocations += location
            detailError?.let { throw it }
            return nextDetail ?: detail(orderId, accepted)
        }

        override suspend fun acceptOrder(orderId: String): AvailableOrderDetail {
            acceptCalls++
            acceptError?.let { throw it }
            return detail(orderId, accepted = true)
        }

        private fun detail(id: String, accepted: Boolean) = AvailableOrderDetail(
            id = id,
            status = OrderStatus.Pending,
            zone = "Víctor Larco Herrera",
            items = listOf(OrderItem("Cartón", 10.0)),
            totalWeightKg = 10.0,
            distanceKm = 1.2,
            createdAt = null,
            acceptanceCount = if (accepted) 1 else 0,
            acceptanceStatus = if (accepted) AcceptanceStatus.Accepted else null,
            photos = emptyList(),
            neighbor = if (accepted) OrderContact("Ana Torres", "948120337") else null,
        )
    }

    private companion object {
        const val NoConnectionMessage = "No tienes conexión. Revisa tu red e inténtalo de nuevo."
    }
}
