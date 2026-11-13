package pe.edu.upn.reciclatru.feature.orders.domain

import javax.inject.Inject

class GetMyOrdersUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(tab: OrdersTab, cursor: String? = null): OrdersPage =
        repository.getMyOrders(tab, cursor)
}
