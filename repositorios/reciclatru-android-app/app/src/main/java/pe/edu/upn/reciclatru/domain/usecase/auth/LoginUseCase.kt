package pe.edu.upn.reciclatru.domain.usecase.auth

import pe.edu.upn.reciclatru.domain.repository.auth.AuthRepository
import pe.edu.upn.reciclatru.model.auth.LoginResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): LoginResult =
        authRepository.login(email = email.trim(), password = password)
}
