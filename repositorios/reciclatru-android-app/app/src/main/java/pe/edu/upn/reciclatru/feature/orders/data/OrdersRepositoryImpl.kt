package pe.edu.upn.reciclatru.feature.orders.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersPage
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrdersRepositoryImpl @Inject constructor(
    private val api: OrdersApi,
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

    private fun unexpectedResponse() = NetworkException(
        kind = NetworkErrorKind.Unknown,
        message = "No pudimos completar la operación. Inténtalo de nuevo.",
    )

    private companion object {
        const val PageSize = 20
    }
}
