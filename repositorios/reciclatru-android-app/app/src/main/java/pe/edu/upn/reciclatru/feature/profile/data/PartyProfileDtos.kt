package pe.edu.upn.reciclatru.feature.profile.data

import com.google.gson.annotations.SerializedName

data class ProfileUpdateRequestDto(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("reference") val reference: String? = null,
)

data class PartyProfileOutputDto(
    @SerializedName("crReferenceId") val crReferenceId: String? = null,
    @SerializedName("partyDataManagementInstanceRecord") val record: PartyProfileRecordDto? = null,
)

data class PartyProfileRecordDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("reference") val reference: String? = null,
    @SerializedName("pickupPoint") val pickupPoint: ProfilePickupPointDto? = null,
    @SerializedName("profileComplete") val profileComplete: Boolean? = null,
)

data class ProfilePickupPointDto(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("district") val district: String? = null,
)
