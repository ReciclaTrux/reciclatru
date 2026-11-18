package pe.edu.upn.reciclatru.domain.repository.orders

interface PendingPhotosStore {
    fun get(orderId: String): List<String>
    fun put(orderId: String, photos: List<String>)
}
