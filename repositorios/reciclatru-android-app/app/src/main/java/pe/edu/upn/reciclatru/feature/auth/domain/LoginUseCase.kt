package pe.edu.upn.reciclatru.feature.auth.domain

import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): LoginResult =
        authRepository.login(email = email.trim(), password = password)
}
