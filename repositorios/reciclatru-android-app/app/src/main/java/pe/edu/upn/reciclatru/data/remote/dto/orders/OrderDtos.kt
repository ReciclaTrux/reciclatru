package pe.edu.upn.reciclatru.data.remote.dto.orders

import com.google.gson.annotations.SerializedName

data class OrderItemDto(
    @SerializedName("description") val description: String? = null,
    @SerializedName("weightKg") val weightKg: Double? = null,
)

data class ServicingOrderInitiateRequestDto(
    @SerializedName("items") val items: List<OrderItemDto>,
)

data class ServicingOrderUpdateRequestDto(
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
    @SerializedName("counterpart") val counterpart: CounterpartDto? = null,
    @SerializedName("availableActions") val availableActions: List<String>? = null,
    @SerializedName("photos") val photos: List<OrderPhotoDto>? = null,
    @SerializedName("zone") val zone: String? = null,
    @SerializedName("approximateDistanceKm") val approximateDistanceKm: Double? = null,
    @SerializedName("alreadyAccepted") val alreadyAccepted: Boolean? = null,
    @SerializedName("acceptanceStatus") val acceptanceStatus: String? = null,
    @SerializedName("pickup") val pickup: PickupDetailDto? = null,
)

data class PickupDetailDto(
    @SerializedName("address") val address: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
)

data class OrderPhotoDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("url") val url: String? = null,
)

data class CounterpartDto(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
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
    @SerializedName("thumbnailUrl") val thumbnailUrl: String? = null,
)

data class AcceptanceListOutputDto(
    @SerializedName("items") val items: List<AcceptanceRecordDto>? = null,
)

data class AcceptanceRecordDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("acceptedAt") val acceptedAt: String? = null,
)

data class ChooseRecyclerRequestDto(
    @SerializedName("acceptanceId") val acceptanceId: String,
)
