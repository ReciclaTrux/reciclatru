package pe.edu.upn.reciclatru.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.navigation.AppRoutes
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.auth.domain.LoginUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val login: LoginUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(sessionExpired = savedStateHandle.get<Boolean>(AppRoutes.ExpiredArg) ?: false),
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.submitting) return

        val email = current.email.trim()
        val emailError = when {
            email.isEmpty() -> EmailRequiredMessage
            !EmailPattern.matches(email) -> EmailInvalidMessage
            else -> null
        }
        val passwordError = if (current.password.isEmpty()) PasswordRequiredMessage else null

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError, generalError = null)
            }
            return
        }

        _uiState.update { it.copy(submitting = true, generalError = null) }
        viewModelScope.launch {
            try {
                val result = login(email = email, password = current.password)
                _uiState.update {
                    it.copy(
                        submitting = false,
                        loggedInRole = result.session.role,
                        needsProfile = !result.profileComplete,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(submitting = false, generalError = error.toLoginMessage()) }
            }
        }
    }

    private fun Exception.toLoginMessage(): String {
        val network = this as? NetworkException
        return when {
            network?.kind == NetworkErrorKind.Unauthorized -> InvalidCredentialsMessage
            network?.statusCode == BadRequestCode -> InvalidCredentialsMessage
            network?.statusCode == TooManyRequestsCode -> TooManyRequestsMessage
            else -> toUserMessage()
        }
    }

    private companion object {
        const val BadRequestCode = 400
        const val TooManyRequestsCode = 429
        val EmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val EmailRequiredMessage = "Ingresa tu correo electrónico."
        const val EmailInvalidMessage = "Ingresa un correo válido."
        const val PasswordRequiredMessage = "Ingresa tu contraseña."
        const val InvalidCredentialsMessage = "Correo o contraseña incorrectos."
        const val TooManyRequestsMessage =
            "Hiciste muchos intentos. Espera un momento e inténtalo de nuevo."
    }
}
