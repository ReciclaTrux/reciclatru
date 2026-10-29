package pe.edu.upn.reciclatru.feature.profile.data

import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Operaciones de perfil y ubicación del vecino.
 *
 * `{id}` es el identificador del usuario de la sesión según el modelo de datos. Cuando
 * el contrato OpenAPI esté publicado, se ajusta acá (y solo acá) si el backend espera el
 * id de la ubicación.
 */
interface ProfileApi {

    @PUT("location-data-management/{id}/pickup-point/update")
    suspend fun updatePickupPoint(
        @Path("id") userId: String,
        @Body body: PickupPointRequestDto,
    ): PickupLocationDto
}
