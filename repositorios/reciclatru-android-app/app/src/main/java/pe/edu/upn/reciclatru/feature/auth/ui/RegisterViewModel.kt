package pe.edu.upn.reciclatru.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.domain.LoginUseCase
import pe.edu.upn.reciclatru.feature.auth.domain.RegisterUseCase
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val register: RegisterUseCase,
    private val login: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, generalError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, generalError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun onRoleSelect(role: UserRole) {
        _uiState.update { it.copy(role = role, roleError = null, generalError = null) }
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
        val passwordError = if (current.password.length < MinPasswordLength) PasswordShortMessage else null
        val roleError = if (current.role == null) RoleRequiredMessage else null

        if (emailError != null || passwordError != null || roleError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    roleError = roleError,
                    generalError = null,
                )
            }
            return
        }

        val role = current.role ?: return
        _uiState.update { it.copy(submitting = true, generalError = null) }
        viewModelScope.launch {
            try {
                if (!current.accountCreated) {
                    register(email = email, password = current.password, role = role)
                    _uiState.update { it.copy(accountCreated = true) }
                }
                val session = login(email = email, password = current.password)
                _uiState.update { it.copy(submitting = false, registeredRole = session.role) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { state -> state.withError(error) }
            }
        }
    }

    private fun RegisterUiState.withError(error: Exception): RegisterUiState {
        val network = error as? NetworkException
        return when {
            network?.kind == NetworkErrorKind.Conflict && !accountCreated ->
                copy(submitting = false, emailError = EmailTakenMessage)
            network?.statusCode == TooManyRequestsCode ->
                copy(submitting = false, generalError = TooManyRequestsMessage)
            accountCreated ->
                copy(submitting = false, generalError = LoginAfterRegisterMessage)
            else ->
                copy(submitting = false, generalError = error.toUserMessage())
        }
    }

    private companion object {
        const val MinPasswordLength = 8
        const val TooManyRequestsCode = 429
        val EmailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        const val EmailRequiredMessage = "Ingresa tu correo electrónico."
        const val EmailInvalidMessage = "Ingresa un correo válido."
        const val EmailTakenMessage = "Ya existe una cuenta con este correo"
        const val PasswordShortMessage = "Debe tener al menos 8 caracteres."
        const val RoleRequiredMessage = "Elige cómo usarás ReciclaTru."
        const val TooManyRequestsMessage =
            "Hiciste muchos intentos. Espera un momento e inténtalo de nuevo."
        const val LoginAfterRegisterMessage =
            "Tu cuenta se creó, pero no pudimos ingresar. Toca Crear cuenta para intentarlo de nuevo."
    }
}
