package pe.edu.upn.reciclatru.feature.orders.domain

import java.time.Instant

enum class OrderStatus { Pending, Confirmed, Collected, Cancelled }

data class Order(
    val id: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val totalWeightKg: Double,
    val acceptanceCount: Int? = null,
    val chosenRecyclerName: String? = null,
    val createdAt: Instant? = null,
    val counterpart: OrderContact? = null,
    val canCancel: Boolean = false,
)

data class OrderContact(
    val fullName: String,
    val phone: String,
)
