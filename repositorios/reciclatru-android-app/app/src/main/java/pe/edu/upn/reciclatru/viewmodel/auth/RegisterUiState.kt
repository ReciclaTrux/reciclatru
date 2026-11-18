package pe.edu.upn.reciclatru.viewmodel.auth

import pe.edu.upn.reciclatru.model.session.UserRole

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val role: UserRole? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val roleError: String? = null,
    val generalError: String? = null,
    val submitting: Boolean = false,
    val accountCreated: Boolean = false,
    val registeredRole: UserRole? = null,
)
