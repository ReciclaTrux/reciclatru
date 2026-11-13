package pe.edu.upn.reciclatru.feature.orders.domain

enum class OrderStatus { Pending, Confirmed, Collected, Cancelled }

data class Order(
    val id: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val totalWeightKg: Double,
)
