package pe.edu.upn.reciclatru.feature.orders.domain

interface OrdersRepository {
    suspend fun createOrder(items: List<OrderItem>): Order
}
