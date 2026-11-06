package pe.edu.upn.reciclatru.feature.profile.domain

interface UserProfileRepository {
    suspend fun getProfile(): UserProfile

    suspend fun updateProfile(fullName: String, phone: String, reference: String?): UserProfile
}
