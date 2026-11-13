package pe.edu.upn.reciclatru.feature.orders.ui

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
import pe.edu.upn.reciclatru.feature.orders.domain.GetMyOrdersUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderPhoto
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrderSummary
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersPage
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab

@OptIn(ExperimentalCoroutinesApi::class)
class MyOrdersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `al abrir carga los pedidos activos`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
        assertTrue(viewModel.uiState.value.current.loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OrdersTab.Active, state.selectedTab)
        assertFalse(state.current.loading)
        assertEquals(listOf("a1", "a2"), state.current.orders.map { it.id })
        assertEquals(listOf(OrdersTab.Active to null), repository.calls)
    }

    @Test
    fun `sin pedidos queda la lista vacia y sin error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(active = emptyList())
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.current.loaded)
        assertTrue(viewModel.uiState.value.current.orders.isEmpty())
        assertNull(viewModel.uiState.value.current.error)
    }

    @Test
    fun `si falla la carga muestra el error y reintentar vuelve a cargar`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeOrdersRepository(error = noConnection())
            val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
            advanceUntilIdle()
            assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.current.error)

            repository.error = null
            viewModel.onRetry()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.current.error)
            assertEquals(2, viewModel.uiState.value.current.orders.size)
        }

    @Test
    fun `el historial muestra carga solo la primera vez y luego se actualiza en silencio`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeOrdersRepository()
            val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
            advanceUntilIdle()

            viewModel.onTabSelected(OrdersTab.History)
            advanceUntilIdle()
            viewModel.onTabSelected(OrdersTab.Active)
            advanceUntilIdle()
            viewModel.onTabSelected(OrdersTab.History)
            assertFalse(viewModel.uiState.value.current.loading)
            advanceUntilIdle()

            assertEquals(listOf("h1"), viewModel.uiState.value.current.orders.map { it.id })
            assertEquals(2, repository.calls.count { it.first == OrdersTab.History })
        }

    @Test
    fun `cargar mas agrega la pagina siguiente`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(activeCursor = "cursor-2")
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
        advanceUntilIdle()

        viewModel.onLoadMore()
        viewModel.onLoadMore()
        advanceUntilIdle()

        val state = viewModel.uiState.value.current
        assertEquals(listOf("a1", "a2", "a3"), state.orders.map { it.id })
        assertNull(state.nextCursor)
        assertEquals(listOf(OrdersTab.Active to null, OrdersTab.Active to "cursor-2"), repository.calls)
    }

    @Test
    fun `si falla al actualizar conserva la lista y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
        advanceUntilIdle()

        repository.error = noConnection()
        viewModel.onRefresh()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.current.orders.size)
        assertNull(state.current.error)
        assertFalse(state.current.refreshing)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", state.message)
    }

    @Test
    fun `al volver a la pantalla actualiza la lista sin mostrar carga`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
        advanceUntilIdle()

        viewModel.onResume()
        assertFalse(viewModel.uiState.value.current.loading)
        advanceUntilIdle()

        assertEquals(2, repository.calls.size)
        assertEquals(2, viewModel.uiState.value.current.orders.size)
    }

    @Test
    fun `al publicar un pedido vuelve a activos, recarga y confirma`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(repository), FakePendingPhotosStore())
        advanceUntilIdle()
        viewModel.onTabSelected(OrdersTab.History)
        advanceUntilIdle()

        viewModel.onOrderPublished("new")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OrdersTab.Active, state.selectedTab)
        assertEquals("Pedido publicado", state.message)
        assertEquals("new", state.messageOrderId)
        assertEquals(2, repository.calls.count { it.first == OrdersTab.Active })
    }

    @Test
    fun `si quedaron fotos sin subir el mensaje lo avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val pendingPhotos = FakePendingPhotosStore().apply { put("new", listOf("foto-1", "foto-2")) }
        val viewModel = MyOrdersViewModel(GetMyOrdersUseCase(FakeOrdersRepository()), pendingPhotos)
        advanceUntilIdle()

        viewModel.onOrderPublished("new")
        advanceUntilIdle()

        assertEquals("Pedido publicado, pero no se subieron 2 fotos", viewModel.uiState.value.message)
        assertEquals("new", viewModel.uiState.value.messageOrderId)
    }

    private fun noConnection() = NetworkException(
        NetworkErrorKind.NoConnection,
        "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
    )

    private class FakeOrdersRepository(
        private val active: List<OrderSummary> = listOf(summary("a1"), summary("a2")),
        private val activeCursor: String? = null,
        var error: Exception? = null,
    ) : OrdersRepository {
        val calls = mutableListOf<Pair<OrdersTab, String?>>()

        override suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage {
            calls += tab to cursor
            error?.let { throw it }
            return when {
                tab == OrdersTab.History -> OrdersPage(listOf(summary("h1", OrderStatus.Collected)), null)
                cursor == null -> OrdersPage(active, activeCursor)
                else -> OrdersPage(listOf(summary("a3")), null)
            }
        }

        override suspend fun createOrder(items: List<OrderItem>): Order =
            Order(id = "new", status = OrderStatus.Pending, items = items, totalWeightKg = 0.0)

        override suspend fun getOrder(id: String): Order =
            Order(id = id, status = OrderStatus.Pending, items = emptyList(), totalWeightKg = 0.0)

        override suspend fun cancelOrder(id: String): Order =
            Order(id = id, status = OrderStatus.Cancelled, items = emptyList(), totalWeightKg = 0.0)

        override suspend fun uploadPhoto(orderId: String, photo: String): OrderPhoto =
            OrderPhoto(id = photo, url = photo)
    }
}

private fun summary(id: String, status: OrderStatus = OrderStatus.Pending) = OrderSummary(
    id = id,
    status = status,
    itemCount = 2,
    totalWeightKg = 5.0,
    acceptanceCount = 0,
    chosenRecyclerName = null,
    createdAt = null,
)
