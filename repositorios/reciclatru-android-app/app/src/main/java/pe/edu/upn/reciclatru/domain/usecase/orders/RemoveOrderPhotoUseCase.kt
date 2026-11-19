package pe.edu.upn.reciclatru.domain.usecase.orders

import pe.edu.upn.reciclatru.domain.repository.orders.OrdersRepository
import javax.inject.Inject

class RemoveOrderPhotoUseCase @Inject constructor(
    private val repository: OrdersRepository,
) {
    suspend operator fun invoke(orderId: String, photoId: String) = repository.removePhoto(orderId, photoId)
}
