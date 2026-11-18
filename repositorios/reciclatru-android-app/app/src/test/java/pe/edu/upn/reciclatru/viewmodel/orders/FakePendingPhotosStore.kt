package pe.edu.upn.reciclatru.viewmodel.orders

import pe.edu.upn.reciclatru.domain.repository.orders.PendingPhotosStore

class FakePendingPhotosStore : PendingPhotosStore {
    private val photosByOrder = mutableMapOf<String, List<String>>()

    override fun get(orderId: String): List<String> = photosByOrder[orderId].orEmpty()

    override fun put(orderId: String, photos: List<String>) {
        if (photos.isEmpty()) photosByOrder.remove(orderId) else photosByOrder[orderId] = photos
    }
}
