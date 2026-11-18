package pe.edu.upn.reciclatru.data.remote.dto.health

import com.google.gson.annotations.SerializedName

data class HealthDto(
    @SerializedName("status") val status: String? = null,
    @SerializedName("version") val version: String? = null,
)
