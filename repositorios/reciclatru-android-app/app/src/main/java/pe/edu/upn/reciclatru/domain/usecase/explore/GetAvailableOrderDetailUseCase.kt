package pe.edu.upn.reciclatru.domain.usecase.explore

import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.explore.GeoPoint
import javax.inject.Inject

/** Detalle de un pedido para el reciclador; la ubicación solo calcula la distancia y no se guarda. */
class GetAvailableOrderDetailUseCase @Inject constructor(
    private val repository: ExploreRepository,
) {
    suspend operator fun invoke(orderId: String, location: GeoPoint?): AvailableOrderDetail =
        repository.getOrderDetail(orderId, location)
}
