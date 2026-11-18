package pe.edu.upn.reciclatru.domain.repository.explore

import pe.edu.upn.reciclatru.model.explore.GeoPoint

interface DeviceLocationProvider {
    suspend fun currentLocation(): GeoPoint?
}
