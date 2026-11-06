package pe.edu.upn.reciclatru.feature.auth.data

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String,
)

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
)

data class PartyRecordDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("profileComplete") val profileComplete: Boolean? = null,
)

data class PartyDataManagementOutputDto(
    @SerializedName("crReferenceId") val crReferenceId: String? = null,
    @SerializedName("partyDataManagementInstanceRecord") val record: PartyRecordDto? = null,
)

data class PartyAuthenticationOutputDto(
    @SerializedName("crReferenceId") val crReferenceId: String? = null,
    @SerializedName("accessToken") val accessToken: String? = null,
    @SerializedName("tokenType") val tokenType: String? = null,
    @SerializedName("expiresAt") val expiresAt: String? = null,
    @SerializedName("partyDataManagementInstanceRecord") val record: PartyRecordDto? = null,
)
