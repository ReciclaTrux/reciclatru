package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab
import javax.inject.Inject

class GetMyOrdersUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(tab: OrdersTab, cursor: String? = null): OrdersPage =
        repository.getMyOrders(tab, cursor)
}
