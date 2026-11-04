package pe.edu.upn.reciclatru.feature.auth.domain

import pe.edu.upn.reciclatru.core.session.UserRole
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, role: UserRole) {
        authRepository.register(email = email.trim(), password = password, role = role)
    }
}
