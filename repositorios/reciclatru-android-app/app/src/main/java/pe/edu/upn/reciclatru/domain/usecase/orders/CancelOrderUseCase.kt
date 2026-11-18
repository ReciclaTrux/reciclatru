package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.Order
import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(id: String): Order = repository.cancelOrder(id)
}
