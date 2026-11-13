package pe.edu.upn.reciclatru.feature.explore.data

import com.google.gson.annotations.SerializedName

data class ServicingOrderAvailabilityOutputDto(
    @SerializedName("sortedBy") val sortedBy: String? = null,
    @SerializedName("items") val items: List<AvailableOrderDto>? = null,
    @SerializedName("nextCursor") val nextCursor: String? = null,
)

data class AvailableOrderDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("itemCount") val itemCount: Int? = null,
    @SerializedName("totalWeightKg") val totalWeightKg: Double? = null,
    @SerializedName("zone") val zone: String? = null,
    @SerializedName("approximateDistanceKm") val approximateDistanceKm: Double? = null,
    @SerializedName("alreadyAccepted") val alreadyAccepted: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
)
