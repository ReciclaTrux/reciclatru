package pe.edu.upn.reciclatru.domain.usecase.auth

import pe.edu.upn.reciclatru.domain.repository.auth.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}
