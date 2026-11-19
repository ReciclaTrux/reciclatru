package pe.edu.upn.reciclatru.domain.usecase.explore

import pe.edu.upn.reciclatru.domain.repository.explore.ExploreRepository
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import javax.inject.Inject

/**
 * Acepta un pedido. Un doble toque no duplica la aceptación: el backend responde con la
 * misma aceptación y la app solo muestra el estado resultante.
 */
class AcceptOrderUseCase @Inject constructor(
    private val repository: ExploreRepository,
) {
    suspend operator fun invoke(orderId: String): AvailableOrderDetail = repository.acceptOrder(orderId)
}
