package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderItem
import javax.inject.Inject

/** Guarda los ítems de un pedido Pendiente sin aceptaciones; el backend responde 409 si ya no es editable. */
class UpdateOrderUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String, items: List<OrderItem>): Order =
        repository.updateOrder(orderId, items.map { it.copy(description = it.description.trim()) })
}
