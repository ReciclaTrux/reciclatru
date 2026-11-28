package pe.edu.upn.reciclatru

import android.location.Address
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.util.Locale
import java.util.concurrent.Executors

/** Adaptador Flutter al mismo Geocoder nativo empleado por la app Kotlin. */
class MainActivity : FlutterActivity() {
    private val geocoderExecutor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val geocoder by lazy { Geocoder(this, Locale("es", "PE")) }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            GEOCODER_CHANNEL,
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "isPresent" -> result.success(Geocoder.isPresent())
                "search", "suggest", "describe" -> geocoderExecutor.execute {
                    try {
                        val value = when (call.method) {
                            "search" -> search(call.argument<String>("query").orEmpty())
                            "suggest" -> suggest(call.argument<String>("query").orEmpty())
                            else -> describe(
                                call.argument<Number>("latitude")?.toDouble() ?: 0.0,
                                call.argument<Number>("longitude")?.toDouble() ?: 0.0,
                            )
                        }
                        mainHandler.post { result.success(value) }
                    } catch (error: IOException) {
                        val message = if (call.method == "describe") {
                            "No pudimos leer la dirección del pin."
                        } else {
                            "No pudimos buscar esa dirección."
                        }
                        mainHandler.post { result.error("GEOCODER_IO", message, null) }
                    }
                }
                else -> result.notImplemented()
            }
        }
    }

    /** Busca una coincidencia para centrar el mapa. */
    @Suppress("DEPRECATION")
    private fun search(query: String): Map<String, Double>? =
        geocoder.getFromLocationName(query, 1)?.firstOrNull()?.let {
            mapOf("latitude" to it.latitude, "longitude" to it.longitude)
        }

    /** Devuelve cuatro etiquetas únicas con dirección y distrito. */
    @Suppress("DEPRECATION")
    private fun suggest(query: String): List<Map<String, Any>> =
        geocoder.getFromLocationName(query, 4).orEmpty()
            .mapNotNull { address ->
                val parts = labelParts(address) ?: return@mapNotNull null
                mapOf(
                    "label" to "${parts.first}, ${parts.second}",
                    "latitude" to address.latitude,
                    "longitude" to address.longitude,
                )
            }
            .distinctBy { it["label"] }
            .take(4)

    /** Describe el pin confirmado y conserva la selección manual si no hay coincidencia. */
    @Suppress("DEPRECATION")
    private fun describe(latitude: Double, longitude: Double): Map<String, String>? =
        geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()?.let { address ->
            labelParts(address)?.let { (street, district) ->
                mapOf("address" to street, "district" to district)
            }
        }

    /** Usa calle+número; si falta la calle recurre a la primera línea de dirección. */
    private fun labelParts(address: Address): Pair<String, String>? {
        val street = listOfNotNull(address.thoroughfare, address.subThoroughfare)
            .joinToString(" ").takeIf(String::isNotBlank)
            ?: address.getAddressLine(0)?.takeIf(String::isNotBlank)
            ?: return null
        val district = address.subLocality?.takeIf(String::isNotBlank)
            ?: address.locality?.takeIf(String::isNotBlank)
            ?: address.adminArea?.takeIf(String::isNotBlank)
            ?: return null
        return street to district
    }

    override fun onDestroy() {
        geocoderExecutor.shutdown()
        super.onDestroy()
    }

    private companion object {
        const val GEOCODER_CHANNEL = "pe.edu.upn.reciclatru/geocoder"
    }
}
