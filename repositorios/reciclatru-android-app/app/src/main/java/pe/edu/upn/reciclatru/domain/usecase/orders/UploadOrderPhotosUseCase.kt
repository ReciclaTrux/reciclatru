package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import pe.edu.upn.reciclatru.domain.repository.orders.PendingPhotosStore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class UploadOrderPhotosUseCase @Inject constructor(
    private val repository: OrdersRepository,
    private val pendingPhotos: PendingPhotosStore,
) {
    suspend operator fun invoke(orderId: String, photos: List<String>): Int {
        val failed = mutableListOf<String>()
        photos.forEach { photo ->
            try {
                repository.uploadPhoto(orderId, photo)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                failed += photo
            }
        }
        pendingPhotos.put(orderId, failed)
        return failed.size
    }
}
