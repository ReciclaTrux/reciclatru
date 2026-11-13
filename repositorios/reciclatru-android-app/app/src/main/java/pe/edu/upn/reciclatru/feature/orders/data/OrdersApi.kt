package pe.edu.upn.reciclatru.feature.orders.data

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface OrdersApi {

    @POST("servicing-order/initiation")
    suspend fun createOrder(@Body body: ServicingOrderInitiateRequestDto): ServicingOrderOutputDto

    @GET("servicing-order/retrieval")
    suspend fun getMyOrders(
        @Query("tab") tab: String,
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?,
    ): ServicingOrderListOutputDto

    @GET("servicing-order/{id}/retrieval")
    suspend fun getOrder(@Path("id") id: String): ServicingOrderOutputDto

    @PUT("servicing-order/{id}/termination")
    suspend fun cancelOrder(@Path("id") id: String): ServicingOrderOutputDto

    @Multipart
    @POST("servicing-order/{id}/photo/initiation")
    suspend fun uploadPhoto(@Path("id") id: String, @Part file: MultipartBody.Part): OrderPhotoDto
}
