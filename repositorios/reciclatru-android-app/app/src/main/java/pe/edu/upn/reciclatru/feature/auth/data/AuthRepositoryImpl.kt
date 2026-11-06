package pe.edu.upn.reciclatru.feature.auth.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.core.session.Session
import pe.edu.upn.reciclatru.core.session.SessionStore
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.domain.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionStore: SessionStore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AuthRepository {

    override suspend fun register(email: String, password: String, role: UserRole) {
        withContext(ioDispatcher) {
            apiCall {
                api.register(
                    RegisterRequestDto(email = email, password = password, role = role.apiValue),
                )
            }.getOrElse { error -> throw error }
        }
    }

    override suspend fun login(email: String, password: String): Session =
        withContext(ioDispatcher) {
            val response = apiCall {
                api.login(LoginRequestDto(email = email, password = password))
            }.getOrElse { error -> throw error }
            val session = response.toSession()
                ?: throw NetworkException(
                    kind = NetworkErrorKind.Unknown,
                    message = "No pudimos completar la operación. Inténtalo de nuevo.",
                )
            sessionStore.save(session)
            session
        }
}
