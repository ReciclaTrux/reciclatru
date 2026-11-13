package pe.edu.upn.reciclatru.feature.orders.ui

import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtOrderStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val SpanishPeru = Locale.forLanguageTag("es-PE")
private val TimeFormat = DateTimeFormatter.ofPattern("H:mm", SpanishPeru)
private val DayFormat = DateTimeFormatter.ofPattern("d MMM", SpanishPeru)

fun formatWeightKg(weightKg: Double): String {
    val tenths = (weightKg * 10).roundToInt()
    return if (tenths % 10 == 0) "${tenths / 10} kg" else "${tenths / 10}.${tenths % 10} kg"
}

fun formatItemCount(count: Int): String = if (count == 1) "1 ítem" else "$count ítems"

fun formatOrderDate(
    createdAt: Instant?,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    if (createdAt == null) return "Pedido"
    val created = createdAt.atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    return when (created.toLocalDate()) {
        today -> "Hoy, ${created.format(TimeFormat)}"
        today.minusDays(1) -> "Ayer, ${created.format(TimeFormat)}"
        else -> created.format(DayFormat).replace(".", "")
    }
}

fun formatPublishedDate(
    createdAt: Instant?,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    if (createdAt == null) return "Publicado"
    val created = createdAt.atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    return when (created.toLocalDate()) {
        today -> "Publicado hoy, ${created.format(TimeFormat)}"
        today.minusDays(1) -> "Publicado ayer, ${created.format(TimeFormat)}"
        else -> "Publicado el ${created.format(DayFormat).replace(".", "")}"
    }
}

fun OrderStatus.toRtStatus(): RtOrderStatus = when (this) {
    OrderStatus.Pending -> RtOrderStatus.Pending
    OrderStatus.Confirmed -> RtOrderStatus.Confirmed
    OrderStatus.Collected -> RtOrderStatus.Collected
    OrderStatus.Cancelled -> RtOrderStatus.Cancelled
}
