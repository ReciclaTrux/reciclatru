package pe.edu.upn.reciclatru.feature.orders.domain

import javax.inject.Inject

class GetOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(id: String): Order = repository.getOrder(id)
}
