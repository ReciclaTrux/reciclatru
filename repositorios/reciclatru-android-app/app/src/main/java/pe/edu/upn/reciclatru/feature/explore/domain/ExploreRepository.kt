package pe.edu.upn.reciclatru.feature.explore.domain

interface ExploreRepository {
    suspend fun getAvailableOrders(location: GeoPoint?, cursor: String?): AvailableOrdersPage
}
