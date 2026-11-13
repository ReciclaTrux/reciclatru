package pe.edu.upn.reciclatru.feature.explore.domain

import javax.inject.Inject

class GetAvailableOrdersUseCase @Inject constructor(
    private val repository: ExploreRepository,
) {
    suspend operator fun invoke(location: GeoPoint?, cursor: String? = null): AvailableOrdersPage =
        repository.getAvailableOrders(location, cursor)
}
