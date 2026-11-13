package pe.edu.upn.reciclatru.feature.orders.ui

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
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.feature.orders.domain.CancelOrderUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.GetOrderUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersPage
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab

@OptIn(ExperimentalCoroutinesApi::class)
class OrderDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `al abrir carga el pedido`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository())
        assertTrue(viewModel.uiState.value.loading)

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.loading)
        assertEquals("p1", viewModel.uiState.value.order?.id)
    }

    @Test
    fun `si falla la carga muestra el error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository(getError = noConnection()))

        advanceUntilIdle()

        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.loadError)
        assertNull(viewModel.uiState.value.order)
    }

    @Test
    fun `cancelar pide confirmacion antes de llamar a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onCancelClick()
        assertTrue(viewModel.uiState.value.cancelDialogVisible)
        viewModel.onCancelDismissed()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.cancelDialogVisible)
        assertEquals(0, repository.cancelCalls)
    }

    @Test
    fun `al confirmar el pedido pasa a cancelado y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onCancelClick()
        viewModel.onCancelConfirmed()
        viewModel.onCancelConfirmed()
        assertTrue(viewModel.uiState.value.cancelling)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, repository.cancelCalls)
        assertEquals(OrderStatus.Cancelled, state.order?.status)
        assertFalse(state.order?.canCancel ?: true)
        assertFalse(state.cancelDialogVisible)
        assertEquals("Pedido cancelado", state.message)
    }

    @Test
    fun `si falla la cancelacion avisa y el pedido no cambia`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(cancelError = noConnection())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onCancelClick()
        viewModel.onCancelConfirmed()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(OrderStatus.Pending, state.order?.status)
        assertFalse(state.cancelling)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", state.message)
    }

    @Test
    fun `si el backend responde que ya no se puede cancelar avisa y recarga`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeOrdersRepository(
                cancelError = NetworkException(
                    kind = NetworkErrorKind.Unknown,
                    message = "No pudimos completar la operación.",
                    statusCode = 409,
                    code = "ORDER_NOT_CANCELLABLE",
                ),
            )
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onCancelClick()
            viewModel.onCancelConfirmed()
            advanceUntilIdle()

            assertEquals("Este pedido ya no se puede cancelar.", viewModel.uiState.value.message)
            assertEquals(2, repository.getCalls)
        }

    @Test
    fun `un pedido que no se puede cancelar no abre la confirmacion`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository(status = OrderStatus.Collected))
        advanceUntilIdle()

        viewModel.onCancelClick()

        assertFalse(viewModel.uiState.value.cancelDialogVisible)
    }

    private fun createViewModel(repository: OrdersRepository) = OrderDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf(OrderDetailViewModel.OrderIdArg to "p1")),
        getOrder = GetOrderUseCase(repository),
        cancelOrder = CancelOrderUseCase(repository),
    )

    private fun noConnection() = NetworkException(
        NetworkErrorKind.NoConnection,
        "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
    )

    private class FakeOrdersRepository(
        private val status: OrderStatus = OrderStatus.Pending,
        private val getError: Exception? = null,
        private val cancelError: Exception? = null,
    ) : OrdersRepository {
        var getCalls = 0
        var cancelCalls = 0

        override suspend fun getOrder(id: String): Order {
            getCalls++
            getError?.let { throw it }
            return order(id, status)
        }

        override suspend fun cancelOrder(id: String): Order {
            cancelCalls++
            cancelError?.let { throw it }
            return order(id, OrderStatus.Cancelled)
        }

        override suspend fun createOrder(items: List<OrderItem>): Order = order("new", OrderStatus.Pending)

        override suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage =
            OrdersPage(items = emptyList(), nextCursor = null)

        private fun order(id: String, status: OrderStatus) = Order(
            id = id,
            status = status,
            items = listOf(OrderItem("Cartón", 5.0)),
            totalWeightKg = 5.0,
            canCancel = status == OrderStatus.Pending || status == OrderStatus.Confirmed,
        )
    }
}
