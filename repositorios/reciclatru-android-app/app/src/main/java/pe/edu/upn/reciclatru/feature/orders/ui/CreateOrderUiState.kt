package pe.edu.upn.reciclatru.feature.orders.ui

data class OrderItemInput(
    val id: Long,
    val description: String = "",
    val weight: String = "",
    val error: String? = null,
    val descriptionInvalid: Boolean = false,
    val weightInvalid: Boolean = false,
)

data class CreateOrderUiState(
    val items: List<OrderItemInput> = emptyList(),
    val totalWeightLabel: String = "0 kg",
    val pickupAddress: String? = null,
    val profileIncomplete: Boolean = false,
    val itemsError: String? = null,
    val publishError: String? = null,
    val publishing: Boolean = false,
    val published: Boolean = false,
    val publishedOrderId: String? = null,
)
