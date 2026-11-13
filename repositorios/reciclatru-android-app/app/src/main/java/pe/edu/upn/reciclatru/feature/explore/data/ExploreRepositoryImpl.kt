package pe.edu.upn.reciclatru.feature.explore.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.feature.explore.domain.AvailableOrdersPage
import pe.edu.upn.reciclatru.feature.explore.domain.ExploreRepository
import pe.edu.upn.reciclatru.feature.explore.domain.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepositoryImpl @Inject constructor(
    private val api: ExploreApi,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ExploreRepository {

    override suspend fun getAvailableOrders(location: GeoPoint?, cursor: String?): AvailableOrdersPage =
        withContext(ioDispatcher) {
            apiCall {
                api.getAvailableOrders(
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    limit = PageSize,
                    cursor = cursor,
                )
            }
                .getOrElse { error -> throw error }
                .toAvailableOrdersPage()
        }

    private companion object {
        const val PageSize = 20
    }
}
