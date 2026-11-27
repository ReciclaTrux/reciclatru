package pe.edu.upn.reciclatru.viewmodel.explore

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
import pe.edu.upn.reciclatru.domain.repository.explore.MyAcceptancesRepository
import pe.edu.upn.reciclatru.domain.usecase.explore.GetMyAcceptancesUseCase
import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptance
import pe.edu.upn.reciclatru.model.explore.MyAcceptancesPage

@OptIn(ExperimentalCoroutinesApi::class)
class MyAcceptancesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `al abrir carga los activos con los elegidos primero`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository()
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        assertTrue(viewModel.uiState.value.current.loading)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AcceptancesTab.Active, state.selectedTab)
        assertFalse(state.current.loading)
        assertEquals(listOf("a1", "a2"), state.current.acceptances.map { it.orderId })
        assertEquals(AcceptanceLabel.Chosen, state.current.acceptances.first().label)
        assertEquals(listOf(AcceptancesTab.Active to null), repository.calls)
    }

    @Test
    fun `sin aceptaciones queda la lista vacia y sin error`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository(active = emptyList())
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.current.loaded)
        assertTrue(viewModel.uiState.value.current.acceptances.isEmpty())
        assertNull(viewModel.uiState.value.current.error)
    }

    @Test
    fun `si falla la carga muestra el error y reintentar vuelve a cargar`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeMyAcceptancesRepository(error = noConnection())
            val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
            advanceUntilIdle()
            assertEquals(
                "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
                viewModel.uiState.value.current.error,
            )

            repository.error = null
            viewModel.onRetry()
            advanceUntilIdle()

            assertNull(viewModel.uiState.value.current.error)
            assertEquals(2, viewModel.uiState.value.current.acceptances.size)
        }

    @Test
    fun `el historial se carga al cambiar de pestana`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository()
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        advanceUntilIdle()

        viewModel.onTabSelected(AcceptancesTab.History)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(AcceptancesTab.History, state.selectedTab)
        assertEquals(listOf("h1"), state.current.acceptances.map { it.orderId })
        assertEquals(2, state.active.acceptances.size)
    }

    @Test
    fun `deslizar hacia abajo actualiza la lista`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository()
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        advanceUntilIdle()

        repository.active = listOf(acceptance("a2", AcceptanceLabel.Chosen))
        viewModel.onRefresh()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.current.refreshing)
        assertEquals(listOf("a2"), viewModel.uiState.value.current.acceptances.map { it.orderId })
    }

    @Test
    fun `si falla al actualizar mantiene la lista y avisa`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository()
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        advanceUntilIdle()

        repository.error = noConnection()
        viewModel.onRefresh()
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.current.acceptances.size)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", viewModel.uiState.value.message)
    }

    @Test
    fun `al volver a la pantalla se actualiza en silencio`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeMyAcceptancesRepository()
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        advanceUntilIdle()

        repository.active = listOf(acceptance("a3", AcceptanceLabel.AwaitingConfirmation))
        viewModel.onResume()
        assertFalse(viewModel.uiState.value.current.loading)
        advanceUntilIdle()

        assertEquals(listOf("a3"), viewModel.uiState.value.current.acceptances.map { it.orderId })
    }

    @Test
    fun `con cursor carga la siguiente pagina de 20`() = runTest(mainDispatcherRule.testDispatcher) {
        val firstPage = (1..20).map { acceptance("p$it", AcceptanceLabel.AwaitingConfirmation) }
        val repository = FakeMyAcceptancesRepository(active = firstPage, nextCursor = "c2")
        val viewModel = MyAcceptancesViewModel(GetMyAcceptancesUseCase(repository))
        advanceUntilIdle()

        repository.active = listOf(acceptance("p21", AcceptanceLabel.AwaitingConfirmation))
        repository.nextCursor = null
        viewModel.onLoadMore()
        advanceUntilIdle()

        val current = viewModel.uiState.value.current
        assertEquals(21, current.acceptances.size)
        assertNull(current.nextCursor)
        assertEquals(AcceptancesTab.Active to "c2", repository.calls.last())
    }

    private fun noConnection() = NetworkException(
        kind = NetworkErrorKind.NoConnection,
        message = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
    )
}

private fun acceptance(id: String, label: AcceptanceLabel) = MyAcceptance(
    orderId = id,
    label = label,
    zone = "Trujillo",
    totalWeightKg = 5.0,
    acceptedAt = null,
)

private class FakeMyAcceptancesRepository(
    var active: List<MyAcceptance> = listOf(
        acceptance("a1", AcceptanceLabel.Chosen),
        acceptance("a2", AcceptanceLabel.AwaitingConfirmation),
    ),
    var history: List<MyAcceptance> = listOf(acceptance("h1", AcceptanceLabel.NotChosen)),
    var nextCursor: String? = null,
    var error: Exception? = null,
) : MyAcceptancesRepository {

    val calls = mutableListOf<Pair<AcceptancesTab, String?>>()

    override suspend fun getMyAcceptances(tab: AcceptancesTab, cursor: String?): MyAcceptancesPage {
        calls += tab to cursor
        error?.let { throw it }
        val items = if (tab == AcceptancesTab.Active) active else history
        return MyAcceptancesPage(items = items, nextCursor = nextCursor)
    }
}
