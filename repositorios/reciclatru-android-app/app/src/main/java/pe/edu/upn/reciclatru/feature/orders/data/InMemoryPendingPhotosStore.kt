package pe.edu.upn.reciclatru.feature.orders.data

import pe.edu.upn.reciclatru.feature.orders.domain.PendingPhotosStore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryPendingPhotosStore @Inject constructor() : PendingPhotosStore {

    private val photosByOrder = ConcurrentHashMap<String, List<String>>()

    override fun get(orderId: String): List<String> = photosByOrder[orderId].orEmpty()

    override fun put(orderId: String, photos: List<String>) {
        if (photos.isEmpty()) photosByOrder.remove(orderId) else photosByOrder[orderId] = photos
    }
}
