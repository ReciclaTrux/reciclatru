package pe.edu.upn.reciclatru.domain.usecase.auth

import pe.edu.upn.reciclatru.domain.repository.auth.AuthRepository
import pe.edu.upn.reciclatru.model.session.UserRole
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String, role: UserRole) {
        authRepository.register(email = email.trim(), password = password, role = role)
    }
}
