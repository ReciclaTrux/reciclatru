package pe.edu.upn.reciclatru.feature.auth.ui

import pe.edu.upn.reciclatru.core.session.UserRole

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val passwordVisible: Boolean = false,
    val sessionExpired: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val submitting: Boolean = false,
    val loggedInRole: UserRole? = null,
    val needsProfile: Boolean = false,
)
