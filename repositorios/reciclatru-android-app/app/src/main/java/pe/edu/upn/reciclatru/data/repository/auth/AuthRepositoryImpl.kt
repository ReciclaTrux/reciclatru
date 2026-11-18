package pe.edu.upn.reciclatru.data.repository.auth

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.local.session.SessionStore
import pe.edu.upn.reciclatru.data.remote.NetworkErrorKind
import pe.edu.upn.reciclatru.data.remote.NetworkException
import pe.edu.upn.reciclatru.data.remote.api.auth.AuthApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.dto.auth.LoginRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.auth.RegisterRequestDto
import pe.edu.upn.reciclatru.data.remote.mapper.auth.toSession
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.auth.AuthRepository
import pe.edu.upn.reciclatru.model.auth.LoginResult
import pe.edu.upn.reciclatru.model.session.SignOutReason
import pe.edu.upn.reciclatru.model.session.UserRole
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

    override suspend fun login(email: String, password: String): LoginResult =
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
            LoginResult(
                session = session,
                profileComplete = response.record?.profileComplete ?: false,
            )
        }

    override suspend fun logout() {
        sessionStore.clear(SignOutReason.Logout)
    }
}
