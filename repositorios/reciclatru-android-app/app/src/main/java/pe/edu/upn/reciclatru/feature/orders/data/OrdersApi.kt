package pe.edu.upn.reciclatru.feature.orders.data

import retrofit2.http.Body
import retrofit2.http.POST

interface OrdersApi {

    @POST("servicing-order/initiation")
    suspend fun createOrder(@Body body: ServicingOrderInitiateRequestDto): ServicingOrderOutputDto
}
