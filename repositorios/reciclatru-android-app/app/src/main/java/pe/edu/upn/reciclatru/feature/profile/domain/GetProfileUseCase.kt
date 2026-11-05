package pe.edu.upn.reciclatru.feature.profile.domain

import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository,
) {
    suspend operator fun invoke(): UserProfile = repository.getProfile()
}
