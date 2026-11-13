package pe.edu.upn.reciclatru.feature.orders.domain

import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(items: List<OrderItem>): Order =
        repository.createOrder(items.map { it.copy(description = it.description.trim()) })
}
