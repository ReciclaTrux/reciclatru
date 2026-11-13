package pe.edu.upn.reciclatru.feature.orders.domain

interface OrdersRepository {
    suspend fun createOrder(items: List<OrderItem>): Order
    suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage
    suspend fun getOrder(id: String): Order
    suspend fun cancelOrder(id: String): Order
}
