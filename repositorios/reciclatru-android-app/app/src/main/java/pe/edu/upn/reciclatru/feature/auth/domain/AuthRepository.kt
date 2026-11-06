package pe.edu.upn.reciclatru.feature.auth.domain

import pe.edu.upn.reciclatru.core.session.UserRole

interface AuthRepository {
    suspend fun register(email: String, password: String, role: UserRole)

    suspend fun login(email: String, password: String): LoginResult

    suspend fun logout()
}
