package pe.edu.upn.reciclatru.feature.orders.domain

import javax.inject.Inject

class CancelOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(id: String): Order = repository.cancelOrder(id)
}
