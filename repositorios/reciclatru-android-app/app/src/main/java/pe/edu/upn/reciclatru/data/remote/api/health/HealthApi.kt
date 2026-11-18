package pe.edu.upn.reciclatru.data.remote.api.health

import pe.edu.upn.reciclatru.data.remote.dto.health.HealthDto
import retrofit2.http.GET

interface HealthApi {

    @GET("health")
    suspend fun check(): HealthDto
}
