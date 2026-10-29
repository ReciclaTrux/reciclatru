package pe.edu.upn.reciclatru.feature.profile.data

import pe.edu.upn.reciclatru.feature.profile.domain.AddressSuggestion
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.GeocodingRepository
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import javax.inject.Inject
import javax.inject.Singleton

/** Traduce el Geocoder del sistema al contrato de `domain`. */
@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val dataSource: AndroidGeocoderDataSource,
) : GeocodingRepository {

    override val isAvailable: Boolean
        get() = dataSource.isPresent

    override suspend fun search(query: String): GeoPoint? =
        if (query.isBlank()) null else dataSource.search(query.trim())

    override suspend fun suggest(query: String): List<AddressSuggestion> =
        if (query.isBlank()) emptyList() else dataSource.suggest(query.trim())

    override suspend fun describe(point: GeoPoint): PickupAddress? =
        dataSource.describe(point)
}
