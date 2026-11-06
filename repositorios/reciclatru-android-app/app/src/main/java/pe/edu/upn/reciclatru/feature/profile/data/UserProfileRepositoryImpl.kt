package pe.edu.upn.reciclatru.feature.profile.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfile
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfileRepository
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
