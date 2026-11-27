package pe.edu.upn.reciclatru.view.explore

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AvailableOrderDetail
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceState
import java.time.Instant
import java.time.ZoneId

class AcceptanceFormatTest {

    private val zone = ZoneId.of("America/Lima")
    private val now = Instant.parse("2026-11-20T20:00:00Z")

    @Test
    fun `la fecha de aceptacion dice hoy, ayer o el dia`() {
        assertEquals("Aceptado hoy, 9:15", formatAcceptedOn(Instant.parse("2026-11-20T14:15:00Z"), now, zone))
        assertEquals("Aceptado ayer, 18:40", formatAcceptedOn(Instant.parse("2026-11-19T23:40:00Z"), now, zone))
        assertEquals("Aceptado el 12 nov", formatAcceptedOn(Instant.parse("2026-11-12T15:00:00Z"), now, zone))
        assertEquals("Aceptado", formatAcceptedOn(null, now, zone))
    }

    @Test
    fun `cada etiqueta usa su chip`() {
        assertEquals(RtAcceptanceState.Waiting, AcceptanceLabel.AwaitingConfirmation.toRtAcceptanceState())
        assertEquals(RtAcceptanceState.Chosen, AcceptanceLabel.Chosen.toRtAcceptanceState())
        assertEquals(RtAcceptanceState.NotChosen, AcceptanceLabel.NotChosen.toRtAcceptanceState())
        assertEquals(RtAcceptanceState.CancelledByNeighbor, AcceptanceLabel.CancelledByNeighbor.toRtAcceptanceState())
        assertEquals(RtAcceptanceState.Collected, AcceptanceLabel.Collected.toRtAcceptanceState())
    }

    @Test
    fun `el detalle combina el estado de la aceptacion y del pedido`() {
        assertEquals(RtAcceptanceState.Waiting, detail(OrderStatus.Pending, AcceptanceStatus.Accepted).acceptanceState())
        assertEquals(RtAcceptanceState.Chosen, detail(OrderStatus.Confirmed, AcceptanceStatus.Chosen).acceptanceState())
        assertEquals(RtAcceptanceState.NotChosen, detail(OrderStatus.Confirmed, AcceptanceStatus.Discarded).acceptanceState())
        assertEquals(
            RtAcceptanceState.CancelledByNeighbor,
            detail(OrderStatus.Cancelled, AcceptanceStatus.Accepted).acceptanceState(),
        )
        assertEquals(RtAcceptanceState.Collected, detail(OrderStatus.Collected, AcceptanceStatus.Chosen).acceptanceState())
    }

    private fun detail(status: OrderStatus, acceptance: AcceptanceStatus) = AvailableOrderDetail(
        id = "o1",
        status = status,
        zone = "Trujillo",
        items = emptyList(),
        totalWeightKg = 4.0,
        distanceKm = null,
        createdAt = null,
        acceptanceCount = 1,
        acceptanceStatus = acceptance,
        photos = emptyList(),
        neighbor = null,
    )
}
