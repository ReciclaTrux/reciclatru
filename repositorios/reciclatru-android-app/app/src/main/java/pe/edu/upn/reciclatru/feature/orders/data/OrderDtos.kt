package pe.edu.upn.reciclatru.feature.orders.data

import com.google.gson.annotations.SerializedName

data class OrderItemDto(
    @SerializedName("description") val description: String? = null,
    @SerializedName("weightKg") val weightKg: Double? = null,
)

data class ServicingOrderInitiateRequestDto(
    @SerializedName("items") val items: List<OrderItemDto>,
)

data class ServicingOrderOutputDto(
    @SerializedName("crReferenceId") val crReferenceId: String? = null,
    @SerializedName("servicingOrderInstanceRecord") val record: ServicingOrderRecordDto? = null,
)

data class ServicingOrderRecordDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("items") val items: List<OrderItemDto>? = null,
    @SerializedName("totalWeightKg") val totalWeightKg: Double? = null,
    @SerializedName("acceptanceCount") val acceptanceCount: Int? = null,
    @SerializedName("chosenRecyclerName") val chosenRecyclerName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)

data class ServicingOrderListOutputDto(
    @SerializedName("items") val items: List<OrderSummaryDto>? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null,
)

data class OrderSummaryDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("itemCount") val itemCount: Int? = null,
    @SerializedName("totalWeightKg") val totalWeightKg: Double? = null,
    @SerializedName("acceptanceCount") val acceptanceCount: Int? = null,
    @SerializedName("chosenRecyclerName") val chosenRecyclerName: String? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)
