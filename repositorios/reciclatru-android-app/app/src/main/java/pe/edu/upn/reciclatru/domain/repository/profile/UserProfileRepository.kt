package pe.edu.upn.reciclatru.domain.repository.profile

import pe.edu.upn.reciclatru.model.profile.UserProfile

interface UserProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(fullName: String, phone: String, reference: String?): UserProfile
}
