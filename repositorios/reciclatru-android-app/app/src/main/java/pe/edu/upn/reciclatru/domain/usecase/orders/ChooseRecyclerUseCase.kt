package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.Order
import javax.inject.Inject

/**
 * Elige a un reciclador. El backend confirma el pedido y descarta a los demás en una sola
 * transacción; la app no decide nada de eso.
 */
class ChooseRecyclerUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String, acceptanceId: String): Order =
        repository.chooseRecycler(orderId, acceptanceId)
}
