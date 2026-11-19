package pe.edu.upn.reciclatru.viewmodel.orders



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
import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.domain.repository.orders.PendingPhotosStore
import pe.edu.upn.reciclatru.domain.usecase.orders.CancelOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.ChooseRecyclerUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderAcceptancesUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UploadOrderPhotosUseCase
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab



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

    @Test
    fun `si hay fotos sin subir las muestra como pendientes`() = runTest(mainDispatcherRule.testDispatcher) {
        val pendingPhotos = FakePendingPhotosStore().apply { put("p1", listOf("foto-1", "foto-2")) }
        val viewModel = createViewModel(FakeOrdersRepository(), pendingPhotos)

        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.pendingPhotoCount)
    }

    @Test
    fun `reintentar sube las fotos pendientes y recarga el pedido`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val pendingPhotos = FakePendingPhotosStore().apply { put("p1", listOf("foto-1")) }
        val viewModel = createViewModel(repository, pendingPhotos)
        advanceUntilIdle()

        viewModel.onRetryPhotos()
        assertTrue(viewModel.uiState.value.uploadingPhotos)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.uploadingPhotos)
        assertEquals(0, state.pendingPhotoCount)
        assertEquals("Fotos subidas", state.message)
        assertEquals(listOf("foto-1"), repository.uploadedPhotos)
        assertEquals(2, repository.getCalls)
    }

    @Test
    fun `si el reintento falla las fotos siguen pendientes`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(uploadError = noConnection())
        val pendingPhotos = FakePendingPhotosStore().apply { put("p1", listOf("foto-1")) }
        val viewModel = createViewModel(repository, pendingPhotos)
        advanceUntilIdle()

        viewModel.onRetryPhotos()
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.pendingPhotoCount)
        assertEquals("No pudimos subir las fotos. Inténtalo de nuevo.", viewModel.uiState.value.message)
    }

    private fun createViewModel(
        repository: OrdersRepository,
        pendingPhotos: PendingPhotosStore = FakePendingPhotosStore(),
        handle: SavedStateHandle = SavedStateHandle(mapOf(OrderDetailViewModel.OrderIdArg to "p1")),
    ) = OrderDetailViewModel(
        savedStateHandle = handle,
        getOrder = GetOrderUseCase(repository),
        cancelOrder = CancelOrderUseCase(repository),
        getAcceptances = GetOrderAcceptancesUseCase(repository),
        chooseRecycler = ChooseRecyclerUseCase(repository),
        uploadPhotos = UploadOrderPhotosUseCase(repository, pendingPhotos),
        pendingPhotos = pendingPhotos,
    )

    @Test
    fun `al volver de editar muestra el mensaje y recarga el pedido`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val handle = SavedStateHandle(mapOf(OrderDetailViewModel.OrderIdArg to "p1"))
        val viewModel = createViewModel(repository, handle = handle)
        advanceUntilIdle()
        val callsBefore = repository.getCalls

        handle[OrderDetailViewModel.EditResultKey] = "Cambios guardados"
        advanceUntilIdle()

        assertEquals("Cambios guardados", viewModel.uiState.value.message)
        assertEquals(callsBefore + 1, repository.getCalls)
        assertNull(handle.get<String>(OrderDetailViewModel.EditResultKey))
    }

    private fun acceptance(id: String, name: String) = OrderAcceptance(
        id = id,
        status = AcceptanceStatus.Accepted,
        fullName = name,
        phone = "987654321",
        acceptedAt = null,
    )

    private fun notPending() = NetworkException(
        NetworkErrorKind.Conflict,
        "El pedido ya no está pendiente.",
        statusCode = 409,
        code = "ORDER_NOT_PENDING",
    )

    @Test
    fun `un pedido pendiente carga los recicladores que aceptaron`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(acceptances = listOf(acceptance("a1", "Juan Pérez")))
        val viewModel = createViewModel(repository)

        advanceUntilIdle()

        assertEquals(listOf("a1"), viewModel.uiState.value.acceptances.map { it.id })
        assertFalse(viewModel.uiState.value.acceptancesLoading)
    }

    @Test
    fun `un pedido confirmado no pide aceptaciones`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(status = OrderStatus.Confirmed)
        createViewModel(repository)

        advanceUntilIdle()

        assertEquals(0, repository.acceptanceCalls)
    }

    @Test
    fun `si falla la lista de aceptaciones el pedido sigue visible`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository(acceptancesError = noConnection()))

        advanceUntilIdle()

        assertEquals("p1", viewModel.uiState.value.order?.id)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.acceptancesError)
    }

    @Test
    fun `elegir pide confirmacion antes de llamar a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val juan = acceptance("a1", "Juan Pérez")
        val repository = FakeOrdersRepository(acceptances = listOf(juan))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onChooseClick(juan)

        assertEquals(juan, viewModel.uiState.value.recyclerToChoose)
        assertTrue(repository.chosen.isEmpty())

        viewModel.onChooseDismissed()
        assertNull(viewModel.uiState.value.recyclerToChoose)
        assertTrue(repository.chosen.isEmpty())
    }

    @Test
    fun `confirmar la eleccion deja el pedido confirmado`() = runTest(mainDispatcherRule.testDispatcher) {
        val juan = acceptance("a1", "Juan Pérez")
        val repository = FakeOrdersRepository(acceptances = listOf(juan))
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onChooseClick(juan)
        viewModel.onChooseConfirmed()
        advanceUntilIdle()

        assertEquals(listOf("p1" to "a1"), repository.chosen)
        assertEquals(OrderStatus.Confirmed, viewModel.uiState.value.order?.status)
        assertNull(viewModel.uiState.value.recyclerToChoose)
        assertFalse(viewModel.uiState.value.choosing)
        assertTrue(viewModel.uiState.value.acceptances.isEmpty())
        assertEquals("Elegiste a Juan", viewModel.uiState.value.message)
    }

    @Test
    fun `un segundo toque mientras hay un reciclador por confirmar se ignora`() = runTest(mainDispatcherRule.testDispatcher) {
        val juan = acceptance("a1", "Juan Pérez")
        val maria = acceptance("a2", "María Ríos")
        val viewModel = createViewModel(FakeOrdersRepository(acceptances = listOf(juan, maria)))
        advanceUntilIdle()

        viewModel.onChooseClick(juan)
        viewModel.onChooseClick(maria)

        assertEquals(juan, viewModel.uiState.value.recyclerToChoose)
    }

    @Test
    fun `si la API falla el pedido sigue pendiente y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val juan = acceptance("a1", "Juan Pérez")
        val repository = FakeOrdersRepository(acceptances = listOf(juan), chooseError = noConnection())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onChooseClick(juan)
        viewModel.onChooseConfirmed()
        advanceUntilIdle()

        assertEquals(OrderStatus.Pending, viewModel.uiState.value.order?.status)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.choosing)
    }

    @Test
    fun `si el pedido ya no esta pendiente recarga el estado actual`() = runTest(mainDispatcherRule.testDispatcher) {
        val juan = acceptance("a1", "Juan Pérez")
        val repository = FakeOrdersRepository(acceptances = listOf(juan), chooseError = notPending())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val callsBefore = repository.getCalls

        viewModel.onChooseClick(juan)
        viewModel.onChooseConfirmed()
        advanceUntilIdle()

        assertEquals("Este pedido ya no está pendiente.", viewModel.uiState.value.message)
        assertEquals(callsBefore + 1, repository.getCalls)
    }

    private fun noConnection() = NetworkException(
        NetworkErrorKind.NoConnection,
        "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
    )

    private class FakeOrdersRepository(
        private val status: OrderStatus = OrderStatus.Pending,
        private val getError: Exception? = null,
        private val cancelError: Exception? = null,
        private val uploadError: Exception? = null,
        var acceptances: List<OrderAcceptance> = emptyList(),
        var chooseError: Exception? = null,
        var acceptancesError: Exception? = null,
    ) : OrdersRepository {
        val uploadedPhotos = mutableListOf<String>()
        val chosen = mutableListOf<Pair<String, String>>()
        var acceptanceCalls = 0
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

        override suspend fun updateOrder(id: String, items: List<OrderItem>): Order =
            order(id, status)

        override suspend fun removePhoto(orderId: String, photoId: String) = Unit

        override suspend fun getAcceptances(orderId: String): List<OrderAcceptance> {
            acceptanceCalls++
            acceptancesError?.let { throw it }
            return acceptances
        }

        override suspend fun chooseRecycler(orderId: String, acceptanceId: String): Order {
            chosen += orderId to acceptanceId
            chooseError?.let { throw it }
            return order(orderId, OrderStatus.Confirmed)
        }

        override suspend fun uploadPhoto(orderId: String, photo: String): OrderPhoto {
            uploadError?.let { throw it }
            uploadedPhotos += photo
            return OrderPhoto(id = photo, url = photo)
        }

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
