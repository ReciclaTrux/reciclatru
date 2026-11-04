package pe.edu.upn.reciclatru.feature.auth.domain

import pe.edu.upn.reciclatru.core.session.Session
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Session =
        authRepository.login(email = email.trim(), password = password)
}
