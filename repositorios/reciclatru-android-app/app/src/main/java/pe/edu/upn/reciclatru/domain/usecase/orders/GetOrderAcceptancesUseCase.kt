package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import javax.inject.Inject

/** Recicladores que aceptaron el pedido, de la primera a la última aceptación. */
class GetOrderAcceptancesUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String): List<OrderAcceptance> = repository.getAcceptances(orderId)
}
