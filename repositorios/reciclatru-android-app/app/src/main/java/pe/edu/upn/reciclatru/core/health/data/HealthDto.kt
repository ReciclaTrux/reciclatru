package pe.edu.upn.reciclatru.core.health.data

import com.google.gson.annotations.SerializedName

data class HealthDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("version") val version: String? = null,
)
