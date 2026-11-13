package pe.edu.upn.reciclatru.feature.orders.domain

import java.time.Instant

enum class OrdersTab { Active, History }

data class OrderSummary(
    val id: String,
    val status: OrderStatus,
    val itemCount: Int,
    val totalWeightKg: Double,
    val acceptanceCount: Int?,
    val chosenRecyclerName: String?,
    val createdAt: Instant?,
)

data class OrdersPage(
    val items: List<OrderSummary>,
    val nextCursor: String?,
)
