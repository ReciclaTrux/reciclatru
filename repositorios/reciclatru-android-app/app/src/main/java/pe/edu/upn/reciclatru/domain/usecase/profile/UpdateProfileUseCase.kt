package pe.edu.upn.reciclatru.domain.usecase.profile

import pe.edu.upn.reciclatru.domain.repository.profile.UserProfileRepository
import pe.edu.upn.reciclatru.model.profile.UserProfile
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: UserProfileRepository,
) {
    suspend operator fun invoke(fullName: String, phone: String, reference: String?): UserProfile =
        repository.updateProfile(
            fullName = fullName.trim(),
            phone = phone.trim(),
            reference = reference?.trim(),
        )
}
