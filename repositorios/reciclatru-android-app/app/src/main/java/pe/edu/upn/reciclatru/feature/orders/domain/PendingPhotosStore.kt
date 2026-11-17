package pe.edu.upn.reciclatru.feature.orders.domain

interface PendingPhotosStore {
    fun get(orderId: String): List<String>
    fun put(orderId: String, photos: List<String>)
}
