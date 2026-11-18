package pe.edu.upn.reciclatru.data.repository.health

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.remote.api.health.HealthApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.health.HealthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val api: HealthApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : HealthRepository {

    override suspend fun isBackendUp(): Boolean = withContext(ioDispatcher) {
        val result = apiCall { api.check() }
        result.getOrElse { error -> throw error }.status == StatusUp
    }

    private companion object {
        const val StatusUp = "UP"
    }
}
