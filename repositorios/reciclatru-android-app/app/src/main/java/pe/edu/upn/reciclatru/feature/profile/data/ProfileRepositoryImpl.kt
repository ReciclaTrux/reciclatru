package pe.edu.upn.reciclatru.feature.profile.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.core.session.SessionStore
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation
import pe.edu.upn.reciclatru.feature.profile.domain.ProfileRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda la ubicación en la API. Sin sesión no se puede: el backend necesita saber de
 * quién es la fila. La pantalla muestra ese error con un mensaje entendible.
 *
 * Aún no existe una operación de lectura de la ubicación en el contrato; cuando se
 * publique, se agrega acá y el mapa arranca desde la ubicación guardada.
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val sessionStore: SessionStore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ProfileRepository {

    override suspend fun savePickupLocation(location: PickupLocation): PickupLocation =
        withContext(ioDispatcher) {
            val session = sessionStore.awaitSession()
                ?: throw NetworkException(
                    kind = NetworkErrorKind.Unauthorized,
                    message = "Inicia sesión para guardar tu ubicación.",
                )

            val result = apiCall {
                api.updatePickupPoint(
                    userId = session.userId,
                    body = location.toRequestDto(),
                )
            }
            result.getOrElse { error -> throw error }.toDomain(fallback = location)
        }
}
