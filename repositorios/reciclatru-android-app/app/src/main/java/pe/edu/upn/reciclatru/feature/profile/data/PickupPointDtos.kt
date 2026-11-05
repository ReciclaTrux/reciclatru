package pe.edu.upn.reciclatru.feature.profile.data

import com.google.gson.annotations.SerializedName

/**
 * Cuerpo de `PUT /location-data-management/me/pickup-point/update` según el contrato
 * OpenAPI. La respuesta es el perfil completo ([PartyProfileOutputDto]).
 *
 * Todos los campos llevan `@SerializedName` para que R8 pueda ofuscar el release sin
 * cambiar el JSON.
 */
data class PickupPointRequestDto(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("address") val address: String,
    @SerializedName("district") val district: String,
)
