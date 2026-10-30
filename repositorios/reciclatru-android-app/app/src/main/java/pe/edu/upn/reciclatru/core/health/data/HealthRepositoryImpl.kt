package pe.edu.upn.reciclatru.core.health.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.health.domain.HealthRepository
import pe.edu.upn.reciclatru.core.network.apiCall
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
