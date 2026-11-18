package pe.edu.upn.reciclatru.domain.repository.explore

import pe.edu.upn.reciclatru.model.explore.AvailableOrdersPage
import pe.edu.upn.reciclatru.model.explore.GeoPoint

interface ExploreRepository {
    suspend fun getAvailableOrders(location: GeoPoint?, cursor: String?): AvailableOrdersPage
}
