package pe.edu.upn.reciclatru.feature.profile.data

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/**
 * Operaciones de perfil y ubicación del usuario de la sesión. Según el contrato OpenAPI,
 * `me` reemplaza al id: el backend lo toma del token.
 */
interface ProfileApi {

    @GET("party-data-management/me/retrieval")
    suspend fun getProfile(): PartyProfileOutputDto

    @PUT("party-data-management/me/update")
    suspend fun updateProfile(@Body body: ProfileUpdateRequestDto): PartyProfileOutputDto

    @PUT("location-data-management/me/pickup-point/update")
    suspend fun updatePickupPoint(@Body body: PickupPointRequestDto): PartyProfileOutputDto
}
