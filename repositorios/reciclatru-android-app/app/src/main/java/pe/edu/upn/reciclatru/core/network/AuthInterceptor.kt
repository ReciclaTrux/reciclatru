package pe.edu.upn.reciclatru.core.network

import okhttp3.Interceptor
import okhttp3.Response
import pe.edu.upn.reciclatru.core.session.SessionStore
import javax.inject.Inject

/**
 * Agrega `Authorization: Bearer <token>` cuando hay sesión. Si no la hay, deja pasar la
 * petición: la API responde 401 y la pantalla muestra "Tu sesión venció. Vuelve a
 * ingresar.".
 */
class AuthInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = sessionStore.currentSession?.token
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }
}
