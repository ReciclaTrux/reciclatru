package pe.edu.upn.reciclatru.viewmodel.orders

import pe.edu.upn.reciclatru.model.orders.OrderSummary
import pe.edu.upn.reciclatru.model.orders.OrdersTab

data class OrdersTabState(
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val orders: List<OrderSummary> = emptyList(),
    val nextCursor: String? = null,
    val error: String? = null,
)

data class MyOrdersUiState(
    val selectedTab: OrdersTab = OrdersTab.Active,
    val active: OrdersTabState = OrdersTabState(),
    val history: OrdersTabState = OrdersTabState(),
    val message: String? = null,
    val messageOrderId: String? = null,
) {
    val current: OrdersTabState
        get() = tab(selectedTab)

    fun tab(tab: OrdersTab): OrdersTabState = when (tab) {
        OrdersTab.Active -> active
        OrdersTab.History -> history
    }
}
