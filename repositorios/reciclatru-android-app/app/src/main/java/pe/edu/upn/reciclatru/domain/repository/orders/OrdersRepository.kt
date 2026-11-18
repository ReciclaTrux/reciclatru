package pe.edu.upn.reciclatru.domain.repository.orders

import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab

interface OrdersRepository {
    suspend fun createOrder(items: List<OrderItem>): Order
    suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage
    suspend fun getOrder(id: String): Order
    suspend fun cancelOrder(id: String): Order
    suspend fun uploadPhoto(orderId: String, photo: String): OrderPhoto
}
