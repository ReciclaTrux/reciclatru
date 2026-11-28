package pe.edu.upn.reciclatru.view.explore

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import pe.edu.upn.reciclatru.model.explore.PickupPoint

fun PickupPoint.geoUri(label: String): String =
    "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(label)})"

fun PickupPoint.webDirectionsUrl(): String =
    "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"

fun PickupPoint.copyText(zone: String): String =
    listOfNotNull(address, zone.takeIf { it.isNotBlank() }).joinToString(", ")
        .ifBlank { "$latitude, $longitude" }

/** Abre la app de mapas; sin ella, Google Maps en el navegador. Devuelve false si nada lo abre. */
fun openDirections(context: Context, pickup: PickupPoint, label: String): Boolean {
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse(pickup.geoUri(label))),
        Intent(Intent.ACTION_VIEW, Uri.parse(pickup.webDirectionsUrl())),
    )
    for (intent in intents) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return true
        } catch (error: ActivityNotFoundException) {
            continue
        }
    }
    return false
}

fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("Dirección", text))
}
