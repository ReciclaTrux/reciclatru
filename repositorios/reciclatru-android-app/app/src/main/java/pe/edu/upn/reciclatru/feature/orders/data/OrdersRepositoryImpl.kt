package pe.edu.upn.reciclatru.feature.orders.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import pe.edu.upn.reciclatru.core.di.IoDispatcher
import pe.edu.upn.reciclatru.core.network.NetworkErrorKind
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.apiCall
import pe.edu.upn.reciclatru.feature.orders.domain.Order
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersRepository
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
            ?: throw NetworkException(
                kind = NetworkErrorKind.Unknown,
                message = "No pudimos completar la operación. Inténtalo de nuevo.",
            )
    }
}
