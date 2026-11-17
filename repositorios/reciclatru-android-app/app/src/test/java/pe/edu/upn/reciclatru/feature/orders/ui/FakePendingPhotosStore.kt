package pe.edu.upn.reciclatru.feature.orders.ui

import pe.edu.upn.reciclatru.feature.orders.domain.PendingPhotosStore

class FakePendingPhotosStore : PendingPhotosStore {
    private val photosByOrder = mutableMapOf<String, List<String>>()

    override fun get(orderId: String): List<String> = photosByOrder[orderId].orEmpty()

    override fun put(orderId: String, photos: List<String>) {
        if (photos.isEmpty()) photosByOrder.remove(orderId) else photosByOrder[orderId] = photos
    }
}
