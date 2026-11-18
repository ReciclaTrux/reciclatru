package pe.edu.upn.reciclatru.domain.repository.health

interface HealthRepository {
    suspend fun isBackendUp(): Boolean
}
