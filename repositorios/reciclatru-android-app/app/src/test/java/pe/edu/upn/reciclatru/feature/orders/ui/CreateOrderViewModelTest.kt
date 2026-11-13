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
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.orders.domain.CreateOrderUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersPage
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
import pe.edu.upn.reciclatru.feature.profile.domain.GetProfileUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfile
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfileRepository

@OptIn(ExperimentalCoroutinesApi::class)
class CreateOrderViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `empieza con un item vacio y total cero`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.items.size)
        assertEquals("0 kg", state.totalWeightLabel)
        assertEquals("Av. Larco 1250, Víctor Larco Herrera", state.pickupAddress)
        assertFalse(state.profileIncomplete)
    }

    @Test
    fun `sin punto de recojo avisa que falta completar el perfil`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(profiles = FakeUserProfileRepository(pickupAddress = null))
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileIncomplete)
        assertNull(viewModel.uiState.value.pickupAddress)
    }

    @Test
    fun `publicar sin items muestra el error y no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val orders = FakeOrdersRepository()
        val viewModel = createViewModel(orders)
        advanceUntilIdle()
        viewModel.onRemoveItem(viewModel.uiState.value.items.first().id)

        viewModel.onPublish()
        advanceUntilIdle()

        assertEquals("Agrega al menos un ítem para publicar", viewModel.uiState.value.itemsError)
        assertEquals(0, orders.createCalls)
    }

    @Test
    fun `el total suma los pesos validos mientras se agregan items`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        viewModel.fillFirstItem("Botellas de plástico", "2.5")
        viewModel.addItem("Cartón", "12")
        viewModel.addItem("Latas", "")

        assertEquals("14.5 kg", viewModel.uiState.value.totalWeightLabel)
    }

    @Test
    fun `el peso solo acepta un decimal`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val id = viewModel.uiState.value.items.first().id

        viewModel.onWeightChange(id, "2.5")
        viewModel.onWeightChange(id, "2.55")
        viewModel.onWeightChange(id, "abc")

        assertEquals("2.5", viewModel.uiState.value.items.first().weight)
    }

    @Test
    fun `la descripcion se corta en 100 caracteres`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        val id = viewModel.uiState.value.items.first().id

        viewModel.onDescriptionChange(id, "a".repeat(120))

        assertEquals(100, viewModel.uiState.value.items.first().description.length)
    }

    @Test
    fun `no deja agregar mas de 10 items`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()

        repeat(12) { viewModel.onAddItem() }

        assertEquals(10, viewModel.uiState.value.items.size)
    }

    @Test
    fun `quitar un item actualiza la lista y el total`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        viewModel.fillFirstItem("Botellas", "2.5")
        viewModel.addItem("Cartón", "8")

        viewModel.onRemoveItem(viewModel.uiState.value.items.first().id)

        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals("8 kg", viewModel.uiState.value.totalWeightLabel)
    }

    @Test
    fun `item sin material o con peso fuera de rango marca el error en ese item`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val orders = FakeOrdersRepository()
            val viewModel = createViewModel(orders)
            viewModel.fillFirstItem("Botellas", "2.5")
            viewModel.addItem("", "3")
            viewModel.addItem("Cartón", "501")
            viewModel.addItem("Latas", "0")

            viewModel.onPublish()
            advanceUntilIdle()

            val items = viewModel.uiState.value.items
            val message = "Escribe el material y un peso mayor a 0 y hasta 500 kg"
            assertNull(items[0].error)
            assertEquals(message, items[1].error)
            assertTrue(items[1].descriptionInvalid)
            assertFalse(items[1].weightInvalid)
            assertEquals(message, items[2].error)
            assertTrue(items[2].weightInvalid)
            assertEquals(message, items[3].error)
            assertEquals(0, orders.createCalls)
        }

    @Test
    fun `corregir un item le quita el error`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel()
        viewModel.onPublish()
        val id = viewModel.uiState.value.items.first().id
        assertTrue(viewModel.uiState.value.items.first().descriptionInvalid)

        viewModel.onDescriptionChange(id, "Botellas")

        assertNull(viewModel.uiState.value.items.first().error)
    }

    @Test
    fun `publicar con items validos envia el pedido y confirma`() = runTest(mainDispatcherRule.testDispatcher) {
        val orders = FakeOrdersRepository()
        val viewModel = createViewModel(orders)
        viewModel.fillFirstItem("  Botellas de plástico  ", "4,5")
        viewModel.addItem("Cartón", "500")

        viewModel.onPublish()
        assertTrue(viewModel.uiState.value.publishing)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.publishing)
        assertTrue(state.published)
        assertEquals("0f8a3c52", state.publishedOrderId)
        assertEquals(
            listOf(OrderItem("Botellas de plástico", 4.5), OrderItem("Cartón", 500.0)),
            orders.lastItems,
        )
    }

    @Test
    fun `tocar publicar dos veces envia el pedido una sola vez`() = runTest(mainDispatcherRule.testDispatcher) {
        val orders = FakeOrdersRepository()
        val viewModel = createViewModel(orders)
        viewModel.fillFirstItem("Botellas", "2.5")

        viewModel.onPublish()
        viewModel.onPublish()
        advanceUntilIdle()

        assertEquals(1, orders.createCalls)
    }

    @Test
    fun `si falla la publicacion avisa y conserva los items para reintentar`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val orders = FakeOrdersRepository(
                error = NetworkException(
                    NetworkErrorKind.NoConnection,
                    "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
                ),
            )
            val viewModel = createViewModel(orders)
            viewModel.fillFirstItem("Botellas", "2.5")

            viewModel.onPublish()
            advanceUntilIdle()

            assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.publishError)
            assertEquals("Botellas", viewModel.uiState.value.items.first().description)
            assertFalse(viewModel.uiState.value.published)

            orders.error = null
            viewModel.onPublish()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.published)
            assertEquals(2, orders.createCalls)
        }

    @Test
    fun `si el backend responde perfil incompleto pide completarlo`() = runTest(mainDispatcherRule.testDispatcher) {
        val orders = FakeOrdersRepository(
            error = NetworkException(
                kind = NetworkErrorKind.Unknown,
                message = "No pudimos completar la operación.",
                statusCode = 422,
                code = "PROFILE_INCOMPLETE",
            ),
        )
        val viewModel = createViewModel(orders)
        advanceUntilIdle()
        viewModel.fillFirstItem("Botellas", "2.5")

        viewModel.onPublish()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.profileIncomplete)
        assertNull(viewModel.uiState.value.publishError)
    }

    private fun createViewModel(
        orders: OrdersRepository = FakeOrdersRepository(),
        profiles: UserProfileRepository = FakeUserProfileRepository(),
    ) = CreateOrderViewModel(CreateOrderUseCase(orders), GetProfileUseCase(profiles))

    private fun CreateOrderViewModel.fillFirstItem(description: String, weight: String) {
        val id = uiState.value.items.first().id
        onDescriptionChange(id, description)
        onWeightChange(id, weight)
    }

    private fun CreateOrderViewModel.addItem(description: String, weight: String) {
        onAddItem()
        val id = uiState.value.items.last().id
        onDescriptionChange(id, description)
        onWeightChange(id, weight)
    }

    private class FakeOrdersRepository(var error: Exception? = null) : OrdersRepository {
        var createCalls = 0
        var lastItems: List<OrderItem> = emptyList()

        override suspend fun createOrder(items: List<OrderItem>): Order {
            createCalls++
            lastItems = items
            error?.let { throw it }
            return Order(
                id = "0f8a3c52",
                status = OrderStatus.Pending,
                items = items,
                totalWeightKg = items.sumOf { it.weightKg },
            )
        }

        override suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage =
            OrdersPage(items = emptyList(), nextCursor = null)

        override suspend fun getOrder(id: String): Order =
            Order(id = id, status = OrderStatus.Pending, items = emptyList(), totalWeightKg = 0.0)
    }

    private class FakeUserProfileRepository(
        private val pickupAddress: PickupAddress? = PickupAddress("Av. Larco 1250", "Víctor Larco Herrera"),
    ) : UserProfileRepository {

        override suspend fun getProfile(): UserProfile = UserProfile(
            email = "ana.torres@gmail.com",
            role = UserRole.Neighbor,
            fullName = "Ana Torres",
            phone = "948120337",
            reference = null,
            pickupAddress = pickupAddress,
            profileComplete = pickupAddress != null,
        )

        override suspend fun updateProfile(fullName: String, phone: String, reference: String?): UserProfile =
            getProfile()
    }
}
