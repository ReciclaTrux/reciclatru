package pe.edu.upn.reciclatru.view.explore

import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SpanishPeru = Locale.forLanguageTag("es-PE")
private val TimeFormat = DateTimeFormatter.ofPattern("H:mm", SpanishPeru)
private val DayFormat = DateTimeFormatter.ofPattern("d MMM", SpanishPeru)

fun AcceptanceLabel.toRtAcceptanceState(): RtAcceptanceState = when (this) {
    AcceptanceLabel.AwaitingConfirmation -> RtAcceptanceState.Waiting
    AcceptanceLabel.Chosen -> RtAcceptanceState.Chosen
    AcceptanceLabel.NotChosen -> RtAcceptanceState.NotChosen
    AcceptanceLabel.CancelledByNeighbor -> RtAcceptanceState.CancelledByNeighbor
    AcceptanceLabel.Collected -> RtAcceptanceState.Collected
}

fun AvailableOrderDetail.acceptanceState(): RtAcceptanceState = when {
    acceptanceStatus == AcceptanceStatus.Discarded -> RtAcceptanceState.NotChosen
    status == OrderStatus.Cancelled -> RtAcceptanceState.CancelledByNeighbor
    status == OrderStatus.Collected -> RtAcceptanceState.Collected
    acceptanceStatus == AcceptanceStatus.Chosen -> RtAcceptanceState.Chosen
    else -> RtAcceptanceState.Waiting
}

fun formatAcceptedOn(
    acceptedAt: Instant?,
    now: Instant = Instant.now(),
    zone: ZoneId = ZoneId.systemDefault(),
): String {
    if (acceptedAt == null) return "Aceptado"
    val accepted = acceptedAt.atZone(zone)
    val today = now.atZone(zone).toLocalDate()
    return when (accepted.toLocalDate()) {
        today -> "Aceptado hoy, ${accepted.format(TimeFormat)}"
        today.minusDays(1) -> "Aceptado ayer, ${accepted.format(TimeFormat)}"
        else -> "Aceptado el ${accepted.format(DayFormat).replace(".", "")}"
    }
}
