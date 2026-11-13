package pe.edu.upn.reciclatru.feature.explore.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import pe.edu.upn.reciclatru.feature.explore.domain.DeviceLocationProvider
import pe.edu.upn.reciclatru.feature.explore.domain.GeoPoint
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FusedDeviceLocationProvider @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient,
) : DeviceLocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint? {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return null

        val tokenSource = CancellationTokenSource()
        val current = withTimeoutOrNull(CurrentLocationTimeoutMillis) {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, tokenSource.token)
                .awaitOrNull { tokenSource.cancel() }
        }
        if (current == null) tokenSource.cancel()

        val location = current ?: withTimeoutOrNull(LastLocationTimeoutMillis) { client.lastLocation.awaitOrNull() }
        return location?.let { GeoPoint(latitude = it.latitude, longitude = it.longitude) }
    }

    private suspend fun Task<Location>.awaitOrNull(onCancel: () -> Unit = {}): Location? =
        suspendCancellableCoroutine { continuation ->
            addOnSuccessListener { location -> if (continuation.isActive) continuation.resume(location) }
            addOnFailureListener { if (continuation.isActive) continuation.resume(null) }
            addOnCanceledListener { if (continuation.isActive) continuation.resume(null) }
            continuation.invokeOnCancellation { onCancel() }
        }

    private companion object {
        const val CurrentLocationTimeoutMillis = 15_000L
        const val LastLocationTimeoutMillis = 3_000L
    }
}
