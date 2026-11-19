package pe.edu.upn.reciclatru.data.remote.api.orders



import okhttp3.MultipartBody
import pe.edu.upn.reciclatru.data.remote.dto.orders.AcceptanceListOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ChooseRecyclerRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.OrderPhotoDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderInitiateRequestDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderListOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderUpdateRequestDto
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

    @PUT("servicing-order/{id}/update")
    suspend fun updateOrder(
        @Path("id") id: String,
        @Body body: ServicingOrderUpdateRequestDto,
    ): ServicingOrderOutputDto

    @PUT("servicing-order/{id}/photo/{photoId}/termination")
    suspend fun removePhoto(@Path("id") id: String, @Path("photoId") photoId: String)

    @GET("servicing-order/{id}/acceptance/retrieval")
    suspend fun getAcceptances(@Path("id") id: String): AcceptanceListOutputDto

    @PUT("servicing-order/{id}/confirmation/execution")
    suspend fun chooseRecycler(
        @Path("id") id: String,
        @Body body: ChooseRecyclerRequestDto,
    ): ServicingOrderOutputDto

    @Multipart
    @POST("servicing-order/{id}/photo/initiation")
    suspend fun uploadPhoto(@Path("id") id: String, @Part file: MultipartBody.Part): OrderPhotoDto
}
