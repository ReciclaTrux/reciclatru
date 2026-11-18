package pe.edu.upn.reciclatru.data.remote.api.explore

import pe.edu.upn.reciclatru.data.remote.dto.explore.ServicingOrderAvailabilityOutputDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExploreApi {

    @GET("servicing-order/availability/retrieval")
    suspend fun getAvailableOrders(
        @Query("lat") latitude: Double?,
        @Query("lng") longitude: Double?,
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): ServicingOrderAvailabilityOutputDto
}
