package pe.edu.upn.reciclatru.domain.usecase.profile

import pe.edu.upn.reciclatru.domain.repository.profile.GeocodingRepository
import pe.edu.upn.reciclatru.model.profile.GeoPoint
import pe.edu.upn.reciclatru.model.profile.PickupAddress
import javax.inject.Inject

/** Convierte el punto confirmado en calle y distrito editables. */
class DescribePointUseCase @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
) {
    suspend operator fun invoke(point: GeoPoint): PickupAddress? =
        geocodingRepository.describe(point)
}
