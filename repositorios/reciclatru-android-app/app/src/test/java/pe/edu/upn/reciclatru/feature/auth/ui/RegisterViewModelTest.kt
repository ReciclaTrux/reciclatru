package pe.edu.upn.reciclatru.feature.auth.ui

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
import pe.edu.upn.reciclatru.core.session.Session
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.domain.AuthRepository
import pe.edu.upn.reciclatru.feature.auth.domain.LoginUseCase
import pe.edu.upn.reciclatru.feature.auth.domain.RegisterUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `inicia vacio y sin rol`() = runTest(mainDispatcherRule.testDispatcher) {
        val state = createViewModel(FakeAuthRepository()).uiState.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.role)
        assertFalse(state.submitting)
    }

    @Test
    fun `sin datos muestra los errores y no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ingresa tu correo electrónico.", state.emailError)
        assertEquals("Debe tener al menos 8 caracteres.", state.passwordError)
        assertEquals("Elige cómo usarás ReciclaTru.", state.roleError)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `correo invalido no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)
        viewModel.fill(email = "ana.torres")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals("Ingresa un correo válido.", viewModel.uiState.value.emailError)
        assertEquals(0, repository.registerCalls)
    }

    @Test
    fun `registro correcto inicia sesion con el rol elegido`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)
        viewModel.fill(email = " ana.torres@gmail.com ")

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(UserRole.Neighbor, state.registeredRole)
        assertFalse(state.submitting)
        assertEquals("ana.torres@gmail.com", repository.lastEmail)
        assertEquals(UserRole.Neighbor, repository.lastRole)
        assertEquals(1, repository.loginCalls)
    }

    @Test
    fun `correo ya registrado muestra el error en el campo`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            registerError = NetworkException(NetworkErrorKind.Conflict, "Ese dato ya está en uso.", 409),
        )
        val viewModel = createViewModel(repository)
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ya existe una cuenta con este correo", state.emailError)
        assertNull(state.registeredRole)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `sin conexion muestra el mensaje y conserva lo escrito`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            registerError = NetworkException(
                NetworkErrorKind.NoConnection,
                "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
            ),
        )
        val viewModel = createViewModel(repository)
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No tienes conexión. Revisa tu red e inténtalo de nuevo.", state.generalError)
        assertEquals("ana.torres@gmail.com", state.email)
        assertEquals(UserRole.Neighbor, state.role)
    }

    @Test
    fun `demasiados intentos muestra el aviso`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            registerError = NetworkException(NetworkErrorKind.Unknown, "No pudimos completar la operación.", 429),
        )
        val viewModel = createViewModel(repository)
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.",
            viewModel.uiState.value.generalError,
        )
    }

    @Test
    fun `si falla el ingreso tras crear la cuenta el reintento solo inicia sesion`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeAuthRepository(
                loginError = NetworkException(NetworkErrorKind.Timeout, "La conexión tardó demasiado."),
            )
            val viewModel = createViewModel(repository)
            viewModel.fill()

            viewModel.onSubmit()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.accountCreated)
            assertNull(viewModel.uiState.value.registeredRole)

            repository.loginError = null
            viewModel.onSubmit()
            advanceUntilIdle()

            assertEquals(1, repository.registerCalls)
            assertEquals(2, repository.loginCalls)
            assertEquals(UserRole.Neighbor, viewModel.uiState.value.registeredRole)
        }

    private fun RegisterViewModel.fill(
        email: String = "ana.torres@gmail.com",
        password: String = "reciclo2026",
        role: UserRole = UserRole.Neighbor,
    ) {
        onEmailChange(email)
        onPasswordChange(password)
        onRoleSelect(role)
    }

    private fun createViewModel(repository: AuthRepository) = RegisterViewModel(
        register = RegisterUseCase(repository),
        login = LoginUseCase(repository),
    )

    private class FakeAuthRepository(
        var registerError: Exception? = null,
        var loginError: Exception? = null,
    ) : AuthRepository {
        var registerCalls = 0
        var loginCalls = 0
        var lastEmail: String? = null
        var lastRole: UserRole? = null

        override suspend fun register(email: String, password: String, role: UserRole) {
            registerCalls++
            lastEmail = email
            lastRole = role
            registerError?.let { throw it }
        }

        override suspend fun login(email: String, password: String): Session {
            loginCalls++
            loginError?.let { throw it }
            return Session(userId = "u-1", token = "token", role = lastRole ?: UserRole.Neighbor)
        }
    }
}
