package pe.edu.upn.reciclatru.core.health.ui

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
import pe.edu.upn.reciclatru.core.health.domain.CheckBackendHealthUseCase
import pe.edu.upn.reciclatru.core.health.domain.HealthRepository
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException

@OptIn(ExperimentalCoroutinesApi::class)
class HealthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `inicia verificando la conexion`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeHealthRepository(up = true))

        val state = viewModel.uiState.value

        assertTrue(state.checking)
        assertFalse(state.connected)
        assertNull(state.error)
    }

    @Test
    fun `si el backend responde UP queda conectado`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeHealthRepository(up = true))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.checking)
        assertTrue(state.connected)
        assertNull(state.error)
    }

    @Test
    fun `si no hay red muestra el mensaje del error`() = runTest(mainDispatcherRule.testDispatcher) {
        val error = NetworkException(
            kind = NetworkErrorKind.NoConnection,
            message = "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
        )
        val viewModel = createViewModel(FakeHealthRepository(error = error))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.checking)
        assertFalse(state.connected)
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", state.error)
    }

    @Test
    fun `si el backend no responde UP no queda conectado`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeHealthRepository(up = false))

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.connected)
        assertEquals(
            "Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento.",
            state.error,
        )
    }

    @Test
    fun `reintentar despues de un error vuelve a consultar`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeHealthRepository(
            error = NetworkException(NetworkErrorKind.Timeout, "La conexión tardó demasiado. Inténtalo de nuevo."),
        )
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        repository.error = null
        repository.up = true
        viewModel.onRetry()
        advanceUntilIdle()

        assertEquals(2, repository.calls)
        assertTrue(viewModel.uiState.value.connected)
        assertNull(viewModel.uiState.value.error)
    }

    private fun createViewModel(repository: HealthRepository): HealthViewModel =
        HealthViewModel(CheckBackendHealthUseCase(repository))

    private class FakeHealthRepository(
        var up: Boolean = true,
        var error: Exception? = null,
    ) : HealthRepository {
        var calls = 0

        override suspend fun isBackendUp(): Boolean {
            calls++
            error?.let { throw it }
            return up
        }
    }
}
