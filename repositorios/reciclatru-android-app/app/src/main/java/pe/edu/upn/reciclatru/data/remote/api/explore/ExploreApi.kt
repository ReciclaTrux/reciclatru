package pe.edu.upn.reciclatru.data.remote.api.explore

import pe.edu.upn.reciclatru.data.remote.dto.explore.ServicingOrderAvailabilityOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderOutputDto
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ExploreApi {

    @GET("servicing-order/availability/retrieval")
    suspend fun getAvailableOrders(
        @Query("lat") latitude: Double?,
        @Query("lng") longitude: Double?,
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): ServicingOrderAvailabilityOutputDto

    @GET("servicing-order/{id}/retrieval")
    suspend fun getOrderDetail(
        @Path("id") id: String,
        @Query("lat") latitude: Double?,
        @Query("lng") longitude: Double?,
    ): ServicingOrderOutputDto

    @POST("servicing-order/{id}/acceptance/initiation")
    suspend fun acceptOrder(@Path("id") id: String): ServicingOrderOutputDto
}
