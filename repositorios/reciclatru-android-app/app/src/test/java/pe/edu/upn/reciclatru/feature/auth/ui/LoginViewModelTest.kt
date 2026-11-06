package pe.edu.upn.reciclatru.feature.auth.ui

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
import pe.edu.upn.reciclatru.core.session.Session
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.domain.AuthRepository
import pe.edu.upn.reciclatru.feature.auth.domain.LoginResult
import pe.edu.upn.reciclatru.feature.auth.domain.LoginUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `inicia vacio y sin aviso de sesion expirada`() = runTest(mainDispatcherRule.testDispatcher) {
        val state = createViewModel(FakeAuthRepository()).uiState.value

        assertEquals("", state.email)
        assertFalse(state.sessionExpired)
        assertNull(state.generalError)
    }

    @Test
    fun `al llegar por sesion vencida muestra el aviso`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeAuthRepository(), expired = true)

        assertTrue(viewModel.uiState.value.sessionExpired)
    }

    @Test
    fun `sin datos muestra los errores y no llama a la API`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository()
        val viewModel = createViewModel(repository)

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ingresa tu correo electrónico.", state.emailError)
        assertEquals("Ingresa tu contraseña.", state.passwordError)
        assertEquals(0, repository.loginCalls)
    }

    @Test
    fun `credenciales correctas inician sesion con el rol de la cuenta`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(role = UserRole.Recycler)
        val viewModel = createViewModel(repository)
        viewModel.fill(email = " juan.perez@gmail.com ")

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(UserRole.Recycler, viewModel.uiState.value.loggedInRole)
        assertEquals("juan.perez@gmail.com", repository.lastEmail)
        assertFalse(viewModel.uiState.value.submitting)
        assertFalse(viewModel.uiState.value.needsProfile)
    }

    @Test
    fun `con perfil incompleto pide completarlo`() = runTest(mainDispatcherRule.testDispatcher) {
        val viewModel = createViewModel(FakeAuthRepository(profileComplete = false))
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.needsProfile)
    }

    @Test
    fun `credenciales incorrectas no dicen cual dato fallo`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            error = NetworkException(NetworkErrorKind.Unauthorized, "Tu sesión venció. Vuelve a ingresar.", 401),
        )
        val viewModel = createViewModel(repository)
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Correo o contraseña incorrectos.", state.generalError)
        assertNull(state.loggedInRole)
        assertEquals("ana.torres@gmail.com", state.email)
    }

    @Test
    fun `demasiados intentos muestra el aviso`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            error = NetworkException(NetworkErrorKind.Unknown, "No pudimos completar la operación.", 429),
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
    fun `sin conexion muestra el mensaje de red`() = runTest(mainDispatcherRule.testDispatcher) {
        val repository = FakeAuthRepository(
            error = NetworkException(
                NetworkErrorKind.NoConnection,
                "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
            ),
        )
        val viewModel = createViewModel(repository)
        viewModel.fill()

        viewModel.onSubmit()
        advanceUntilIdle()

        assertEquals(
            "No tienes conexión. Revisa tu red e inténtalo de nuevo.",
            viewModel.uiState.value.generalError,
        )
    }

    private fun LoginViewModel.fill(
        email: String = "ana.torres@gmail.com",
        password: String = "reciclo2026",
    ) {
        onEmailChange(email)
        onPasswordChange(password)
    }

    private fun createViewModel(repository: AuthRepository, expired: Boolean = false) = LoginViewModel(
        login = LoginUseCase(repository),
        savedStateHandle = SavedStateHandle(mapOf("expired" to expired)),
    )

    private class FakeAuthRepository(
        private val role: UserRole = UserRole.Neighbor,
        private val profileComplete: Boolean = true,
        private val error: Exception? = null,
    ) : AuthRepository {
        var loginCalls = 0
        var lastEmail: String? = null

        override suspend fun register(email: String, password: String, role: UserRole) = Unit

        override suspend fun login(email: String, password: String): LoginResult {
            loginCalls++
            lastEmail = email
            error?.let { throw it }
            return LoginResult(
                session = Session(userId = "u-1", token = "token", role = role),
                profileComplete = profileComplete,
            )
        }

        override suspend fun logout() = Unit
    }
}
