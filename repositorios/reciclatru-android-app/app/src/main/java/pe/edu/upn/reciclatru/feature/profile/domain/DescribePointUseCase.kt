package pe.edu.upn.reciclatru.feature.profile.domain

import javax.inject.Inject

/** Convierte el punto confirmado en calle y distrito editables. */
class DescribePointUseCase @Inject constructor(
    private val geocodingRepository: GeocodingRepository,
) {
    suspend operator fun invoke(point: GeoPoint): PickupAddress? =
        geocodingRepository.describe(point)
}
