package pe.edu.upn.reciclatru.data.repository.orders



import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.edu.upn.reciclatru.data.local.orders.PhotoCompressor
import pe.edu.upn.reciclatru.data.remote.NetworkErrorKind
import pe.edu.upn.reciclatru.data.remote.NetworkException
import pe.edu.upn.reciclatru.data.remote.api.orders.OrdersApi
import pe.edu.upn.reciclatru.data.remote.apiCall
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toAcceptances
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toChooseRecyclerRequest
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toInitiateRequest
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrder
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrderPhoto
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toOrdersPage
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toQuery
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toUpdateRequest
import pe.edu.upn.reciclatru.di.IoDispatcher
import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.model.orders.Order
import pe.edu.upn.reciclatru.model.orders.OrderAcceptance
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrdersPage
import pe.edu.upn.reciclatru.model.orders.OrdersTab
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton




@Singleton
class OrdersRepositoryImpl @Inject constructor(
    private val api: OrdersApi,
    private val photoCompressor: PhotoCompressor,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : OrdersRepository {

    override suspend fun createOrder(items: List<OrderItem>): Order = withContext(ioDispatcher) {
        apiCall { api.createOrder(items.toInitiateRequest()) }
            .getOrElse { error -> throw error }
            .toOrder()
            ?: throw unexpectedResponse()
    }

    override suspend fun getMyOrders(tab: OrdersTab, cursor: String?): OrdersPage = withContext(ioDispatcher) {
        apiCall { api.getMyOrders(tab = tab.toQuery(), limit = PageSize, cursor = cursor) }
            .getOrElse { error -> throw error }
            .toOrdersPage()
    }

    override suspend fun getOrder(id: String): Order = withContext(ioDispatcher) {
        apiCall { api.getOrder(id) }
            .getOrElse { error -> throw error }
            .toOrder()
            ?: throw unexpectedResponse()
    }

    override suspend fun cancelOrder(id: String): Order = withContext(ioDispatcher) {
        apiCall { api.cancelOrder(id) }
            .getOrElse { error -> throw error }
            .toOrder()
            ?: throw unexpectedResponse()
    }

    override suspend fun updateOrder(id: String, items: List<OrderItem>): Order = withContext(ioDispatcher) {
        apiCall { api.updateOrder(id, items.toUpdateRequest()) }
            .getOrElse { error -> throw error }
            .toOrder()
            ?: throw unexpectedResponse()
    }

    override suspend fun removePhoto(orderId: String, photoId: String): Unit = withContext(ioDispatcher) {
        apiCall { api.removePhoto(orderId, photoId) }
            .getOrElse { error -> throw error }
    }

    override suspend fun getAcceptances(orderId: String): List<OrderAcceptance> = withContext(ioDispatcher) {
        apiCall { api.getAcceptances(orderId) }
            .getOrElse { error -> throw error }
            .toAcceptances()
    }

    override suspend fun chooseRecycler(orderId: String, acceptanceId: String): Order = withContext(ioDispatcher) {
        apiCall { api.chooseRecycler(orderId, acceptanceId.toChooseRecyclerRequest()) }
            .getOrElse { error -> throw error }
            .toOrder()
            ?: throw unexpectedResponse()
    }

    override suspend fun uploadPhoto(orderId: String, photo: String): OrderPhoto = withContext(ioDispatcher) {
        val bytes = try {
            photoCompressor.compress(photo)
        } catch (error: IOException) {
            throw NetworkException(kind = NetworkErrorKind.Unknown, message = PhotoUnreadableMessage, cause = error)
        }
        val part = MultipartBody.Part.createFormData(
            PhotoPartName,
            PhotoFileName,
            bytes.toRequestBody(PhotoMediaType.toMediaType()),
        )
        apiCall { api.uploadPhoto(orderId, part) }
            .getOrElse { error -> throw error }
            .toOrderPhoto()
            ?: throw unexpectedResponse()
    }

    private fun unexpectedResponse() = NetworkException(
        kind = NetworkErrorKind.Unknown,
        message = "No pudimos completar la operación. Inténtalo de nuevo.",
    )

    private companion object {
        const val PageSize = 20
        const val PhotoPartName = "file"
        const val PhotoFileName = "foto.jpg"
        const val PhotoMediaType = "image/jpeg"
        const val PhotoUnreadableMessage = "No pudimos leer la foto. Elige otra e inténtalo de nuevo."
    }
}
