package pe.edu.upn.reciclatru.feature.explore.domain

interface DeviceLocationProvider {
    suspend fun currentLocation(): GeoPoint?
}
