package pe.edu.upn.reciclatru.feature.orders.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class OrderFormatTest {

    private val lima = ZoneId.of("America/Lima")
    private val now = Instant.parse("2026-11-12T20:00:00Z")

    @Test
    fun `el peso muestra un decimal solo cuando hace falta`() {
        assertEquals("14.5 kg", formatWeightKg(14.5))
        assertEquals("5 kg", formatWeightKg(5.0))
        assertEquals("13.7 kg", formatWeightKg(13.7))
    }

    @Test
    fun `la cantidad de items va en singular o plural`() {
        assertEquals("1 ítem", formatItemCount(1))
        assertEquals("3 ítems", formatItemCount(3))
    }

    @Test
    fun `un pedido de hoy muestra la hora`() {
        assertEquals("Hoy, 9:15", formatOrderDate(Instant.parse("2026-11-12T14:15:00Z"), now, lima))
    }

    @Test
    fun `un pedido de ayer muestra ayer y la hora`() {
        assertEquals("Ayer, 18:40", formatOrderDate(Instant.parse("2026-11-11T23:40:00Z"), now, lima))
    }

    @Test
    fun `un pedido mas antiguo muestra el dia y el mes`() {
        assertEquals("3 nov", formatOrderDate(Instant.parse("2026-11-03T15:00:00Z"), now, lima))
    }

    @Test
    fun `el detalle dice cuando se publico`() {
        assertEquals("Publicado ayer, 18:40", formatPublishedDate(Instant.parse("2026-11-11T23:40:00Z"), now, lima))
        assertEquals("Publicado el 3 nov", formatPublishedDate(Instant.parse("2026-11-03T15:00:00Z"), now, lima))
    }
}
