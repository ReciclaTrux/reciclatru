package pe.edu.upn.reciclatru.data.remote.api.auth

import pe.edu.upn.reciclatru.data.remote.dto.auth.LoginRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.auth.PartyAuthenticationOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.auth.PartyDataManagementOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.auth.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("party-data-management/initiation")
    suspend fun register(@Body body: RegisterRequestDto): PartyDataManagementOutputDto

    @POST("party-authentication/initiation")
    suspend fun login(@Body body: LoginRequestDto): PartyAuthenticationOutputDto
}
