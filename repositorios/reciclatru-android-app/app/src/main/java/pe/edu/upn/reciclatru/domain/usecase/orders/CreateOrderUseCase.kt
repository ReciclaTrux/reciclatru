package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderItem
import javax.inject.Inject

class CreateOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(items: List<OrderItem>): Order =
        repository.createOrder(items.map { it.copy(description = it.description.trim()) })
}
