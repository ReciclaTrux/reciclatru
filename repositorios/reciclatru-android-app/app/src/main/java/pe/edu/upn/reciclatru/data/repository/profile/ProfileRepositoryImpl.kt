package pe.edu.upn.reciclatru.data.repository.profile

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.remote.api.profile.ProfileApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.mapper.profile.toPickupLocation
import pe.edu.upn.reciclatru.data.remote.mapper.profile.toRequestDto
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.profile.ProfileRepository
import pe.edu.upn.reciclatru.model.profile.PickupLocation
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
