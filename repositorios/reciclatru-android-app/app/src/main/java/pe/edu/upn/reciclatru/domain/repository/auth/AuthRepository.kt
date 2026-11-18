package pe.edu.upn.reciclatru.domain.repository.auth

import pe.edu.upn.reciclatru.model.auth.LoginResult
import pe.edu.upn.reciclatru.model.session.UserRole

interface AuthRepository {
    suspend fun register(email: String, password: String, role: UserRole)

    suspend fun login(email: String, password: String): LoginResult

    suspend fun logout()
}
