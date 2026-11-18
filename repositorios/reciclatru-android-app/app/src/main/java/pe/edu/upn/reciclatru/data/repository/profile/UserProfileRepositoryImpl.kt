package pe.edu.upn.reciclatru.data.repository.profile

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.remote.NetworkErrorKind
import pe.edu.upn.reciclatru.data.remote.NetworkException
import pe.edu.upn.reciclatru.data.remote.api.profile.ProfileApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.dto.profile.PartyProfileOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.profile.ProfileUpdateRequestDto
import pe.edu.upn.reciclatru.data.remote.mapper.profile.toUserProfile
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.profile.UserProfileRepository
import pe.edu.upn.reciclatru.model.profile.UserProfile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserProfileRepository {

    override suspend fun getProfile(): UserProfile = withContext(ioDispatcher) {
        apiCall { api.getProfile() }.getOrElse { error -> throw error }.toProfileOrThrow()
    }

    override suspend fun updateProfile(fullName: String, phone: String, reference: String?): UserProfile =
        withContext(ioDispatcher) {
            apiCall {
                api.updateProfile(
                    ProfileUpdateRequestDto(fullName = fullName, phone = phone, reference = reference),
                )
            }.getOrElse { error -> throw error }.toProfileOrThrow()
        }

    private fun PartyProfileOutputDto.toProfileOrThrow(): UserProfile =
        toUserProfile() ?: throw NetworkException(
            kind = NetworkErrorKind.Unknown,
            message = "No pudimos completar la operación. Inténtalo de nuevo.",
        )
}
