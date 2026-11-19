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
import pe.edu.upn.reciclatru.domain.usecase.orders.GetOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.RemoveOrderPhotoUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UpdateOrderUseCase
import pe.edu.upn.reciclatru.domain.usecase.orders.UploadOrderPhotosUseCase
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab

@OptIn(ExperimentalCoroutinesApi::class)
class EditOrderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `al abrir carga los items y las fotos del pedido`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository())
        assertTrue(viewModel.uiState.value.loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.loading)
        assertEquals(listOf("Cartón", "Latas"), state.items.map { it.description })
        assertEquals(listOf("7.5", "2"), state.items.map { it.weight })
        assertEquals("9.5 kg", state.totalWeightLabel)
        assertEquals(2, state.keptPhotos.size)
        assertFalse(state.dirty)
    }

    @Test
    fun `si el pedido ya no es editable avisa y sale`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository(canEdit = false))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals("Este pedido ya no se puede editar", viewModel.uiState.value.finishMessage)
    }

    @Test
    fun `si falla la carga muestra el error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository(getError = noConnection()))

        advanceUntilIdle()

        assertEquals(NoConnectionMessage, viewModel.uiState.value.loadError)
    }

    @Test
    fun `cambiar un item marca cambios y volver a lo original los quita`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository())
        advanceUntilIdle()
        val id = viewModel.uiState.value.items.first().id

        viewModel.onWeightChange(id, "8")
        assertTrue(viewModel.uiState.value.dirty)

        viewModel.onWeightChange(id, "7.5")
        assertFalse(viewModel.uiState.value.dirty)
    }

    @Test
    fun `salir sin cambios no pregunta y con cambios pide descartar`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository())
        advanceUntilIdle()

        assertFalse(viewModel.onBackRequested())
        assertFalse(viewModel.uiState.value.discardDialogVisible)

        viewModel.onAddItem()
        assertTrue(viewModel.onBackRequested())
        assertTrue(viewModel.uiState.value.discardDialogVisible)

        viewModel.onDiscardDismissed()
        assertFalse(viewModel.uiState.value.discardDialogVisible)
    }

    @Test
    fun `guardar aplica las mismas validaciones que publicar`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onAddItem()

        viewModel.onSave()
        advanceUntilIdle()

        val invalid = viewModel.uiState.value.items.last()
        assertTrue(invalid.descriptionInvalid)
        assertTrue(invalid.weightInvalid)
        assertEquals(0, repository.updateCalls)

        viewModel.onDescriptionChange(invalid.id, "Vidrio")
        viewModel.onWeightChange(invalid.id, "600")
        viewModel.onSave()
        advanceUntilIdle()
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun `no se puede quedar sin items`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.uiState.value.items.forEach { viewModel.onRemoveItem(it.id) }

        viewModel.onSave()

        assertEquals("Agrega al menos un ítem para guardar", viewModel.uiState.value.itemsError)
        assertEquals(0, repository.updateCalls)
    }

    @Test
    fun `guardar envia los items y termina con mensaje`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val id = viewModel.uiState.value.items.first().id
        viewModel.onWeightChange(id, "8")

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(listOf(OrderItem("Cartón", 8.0), OrderItem("Latas", 2.0)), repository.lastItems)
        assertTrue(viewModel.uiState.value.finished)
        assertEquals("Cambios guardados", viewModel.uiState.value.finishMessage)
    }

    @Test
    fun `quitar una foto la elimina y agregar una la sube`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository()
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.onPhotoRemoved(0)
        viewModel.onPhotoAdded("file:///nueva.jpg")
        assertEquals(listOf("b"), viewModel.uiState.value.keptPhotos.map { it.id })
        assertTrue(viewModel.uiState.value.dirty)

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(listOf("a"), repository.removedPhotos)
        assertEquals(listOf("file:///nueva.jpg"), repository.uploadedPhotos)
        assertEquals(0, repository.updateCalls)
        assertTrue(viewModel.uiState.value.finished)
    }

    @Test
    fun `el maximo de fotos cuenta las del pedido y las nuevas`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeOrdersRepository())
        advanceUntilIdle()

        viewModel.onPhotoAdded("uno")
        viewModel.onPhotoAdded("dos")

        assertEquals(3, viewModel.uiState.value.photoCount)
        assertEquals(listOf("uno"), viewModel.uiState.value.newPhotos)
    }

    @Test
    fun `si falla el guardado conserva los cambios y reintenta solo lo pendiente`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(removeError = noConnection())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onWeightChange(viewModel.uiState.value.items.first().id, "8")
        viewModel.onPhotoRemoved(0)

        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(NoConnectionMessage, viewModel.uiState.value.saveError)
        assertFalse(viewModel.uiState.value.finished)
        assertEquals("8", viewModel.uiState.value.items.first().weight)
        assertEquals(1, repository.updateCalls)

        repository.removeError = null
        viewModel.onSave()
        advanceUntilIdle()

        assertEquals(1, repository.updateCalls)
        assertEquals(listOf("a"), repository.removedPhotos)
        assertTrue(viewModel.uiState.value.finished)
    }

    @Test
    fun `si mientras editaba alguien acepto avisa y no guarda`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeOrdersRepository(updateError = notEditable())
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        viewModel.onWeightChange(viewModel.uiState.value.items.first().id, "8")

        viewModel.onSave()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.finished)
        assertEquals("Este pedido ya no se puede editar", viewModel.uiState.value.finishMessage)
        assertNull(viewModel.uiState.value.saveError)
    }

    private fun createViewModel(repository: OrdersRepository) = EditOrderViewModel(
        savedStateHandle = SavedStateHandle(mapOf(EditOrderViewModel.OrderIdArg to "p1")),
        getOrder = GetOrderUseCase(repository),
        updateOrder = UpdateOrderUseCase(repository),
        removePhoto = RemoveOrderPhotoUseCase(repository),
        uploadPhotos = UploadOrderPhotosUseCase(repository, NoopPendingPhotosStore()),
    )

    private fun noConnection() = NetworkException(NetworkErrorKind.NoConnection, NoConnectionMessage)

    private fun notEditable() = NetworkException(
        NetworkErrorKind.Conflict,
        "El pedido ya no se puede editar.",
        statusCode = 409,
        code = "ORDER_NOT_EDITABLE",
    )

    private class NoopPendingPhotosStore : PendingPhotosStore {
        override fun get(orderId: String): List<String> = emptyList()
        override fun put(orderId: String, photos: List<String>) = Unit
    }

    private class FakeOrdersRepository(
        private val canEdit: Boolean = true,
        private val getError: Exception? = null,
        private val updateError: Exception? = null,
        var removeError: Exception? = null,
    ) : OrdersRepository {
        var updateCalls = 0
        var lastItems: List<OrderItem> = emptyList()
        val removedPhotos = mutableListOf<String>()
        val uploadedPhotos = mutableListOf<String>()

        override suspend fun getOrder(id: String): Order {
            getError?.let { throw it }
            return order(id)
        }

        override suspend fun updateOrder(id: String, items: List<OrderItem>): Order {
            updateCalls++
            lastItems = items
            updateError?.let { throw it }
            return order(id)
        }

        override suspend fun removePhoto(orderId: String, photoId: String) {
            removeError?.let { throw it }
            removedPhotos += photoId
        }

        override suspend fun uploadPhoto(orderId: String, photo: String): OrderPhoto {
            uploadedPhotos += photo
            return OrderPhoto(id = photo, url = photo)
        }

        override suspend fun createOrder(items: List<OrderItem>): Order = order("new")

        override suspend fun cancelOrder(id: String): Order = order(id)

        override suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage =
            OrdersPage(items = emptyList(), nextCursor = null)

        override suspend fun getAcceptances(orderId: String): List<OrderAcceptance> = emptyList()

        override suspend fun chooseRecycler(orderId: String, acceptanceId: String): Order = order(orderId)

        private fun order(id: String) = Order(
            id = id,
            status = OrderStatus.Pending,
            items = listOf(OrderItem("Cartón", 7.5), OrderItem("Latas", 2.0)),
            totalWeightKg = 9.5,
            acceptanceCount = 0,
            canEdit = canEdit,
            photos = listOf(OrderPhoto("a", "url-a"), OrderPhoto("b", "url-b")),
        )
    }

    private companion object {
        const val NoConnectionMessage = "No tienes conexión. Revisa tu red e inténtalo de nuevo."
    }
}
