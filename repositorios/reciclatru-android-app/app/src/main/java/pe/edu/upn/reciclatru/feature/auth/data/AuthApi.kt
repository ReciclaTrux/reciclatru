package pe.edu.upn.reciclatru.feature.auth.data

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("party-data-management/initiation")
    suspend fun register(@Body body: RegisterRequestDto): PartyDataManagementOutputDto

    @POST("party-authentication/initiation")
    suspend fun login(@Body body: LoginRequestDto): PartyAuthenticationOutputDto
}
