package pe.edu.upn.reciclatru.domain.usecase.explore

import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.model.explore.AvailableOrdersPage
import pe.edu.upn.reciclatru.model.explore.GeoPoint
import javax.inject.Inject

class GetAvailableOrdersUseCase @Inject constructor(
    private val repository: ExploreRepository,
) {
    suspend operator fun invoke(location: GeoPoint?, cursor: String? = null): AvailableOrdersPage =
        repository.getAvailableOrders(location, cursor)
}
