package pe.edu.upn.reciclatru.domain.usecase.profile

import pe.edu.upn.reciclatru.domain.repository.profile.UserProfileRepository
import pe.edu.upn.reciclatru.model.profile.UserProfile
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository,
) {
    suspend operator fun invoke(): UserProfile = repository.getProfile()
}
