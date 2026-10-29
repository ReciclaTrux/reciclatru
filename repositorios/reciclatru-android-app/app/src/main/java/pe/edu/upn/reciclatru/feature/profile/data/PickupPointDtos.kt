package pe.edu.upn.reciclatru.feature.profile.data

import com.google.gson.annotations.SerializedName

/**
 * Cuerpo de `PUT /location-data-management/{id}/pickup-point/update`. Los nombres siguen
 * el modelo de datos (`snake_case`) hasta que exista el contrato OpenAPI.
 *
 * Todos los campos llevan `@SerializedName` para que R8 pueda ofuscar el release sin
 * cambiar el JSON.
 */
data class PickupPointRequestDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("district") val district: String,
    @SerializedName("reference_note") val referenceNote: String? = null,
)

/** Respuesta de la ubicación guardada. Campos opcionales por si la API omite alguno. */
data class PickupLocationDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("district") val district: String? = null,
    @SerializedName("reference_note") val referenceNote: String? = null,
    @SerializedName("is_current") val isCurrent: Boolean? = null,
)
