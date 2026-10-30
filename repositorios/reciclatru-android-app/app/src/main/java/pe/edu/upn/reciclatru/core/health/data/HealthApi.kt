package pe.edu.upn.reciclatru.core.health.data

import retrofit2.http.GET

interface HealthApi {

    @GET("health")
    suspend fun check(): HealthDto
}
