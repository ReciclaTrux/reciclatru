package pe.edu.upn.reciclatru.feature.profile.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.feature.profile.domain.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda la ubicación en la API con `me`: el backend identifica al vecino por el token.
 * Sin sesión, la API responde 401 y la app vuelve al ingreso.
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override suspend fun savePickupLocation(location: PickupLocation): PickupLocation =
        withContext(ioDispatcher) {
            apiCall { api.updatePickupPoint(body = location.toRequestDto()) }
                .getOrElse { error -> throw error }
                .toPickupLocation(fallback = location)
        }
}
