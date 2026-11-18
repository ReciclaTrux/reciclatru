package pe.edu.upn.reciclatru.data.repository.explore

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.data.remote.api.explore.ExploreApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.mapper.explore.toAvailableOrdersPage
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.model.explore.AvailableOrdersPage
import pe.edu.upn.reciclatru.model.explore.GeoPoint
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
