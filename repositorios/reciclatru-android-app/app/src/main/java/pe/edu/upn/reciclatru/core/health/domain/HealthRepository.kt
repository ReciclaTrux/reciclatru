package pe.edu.upn.reciclatru.core.health.domain

interface HealthRepository {
    suspend fun isBackendUp(): Boolean
}
