package pe.edu.upn.reciclatru.feature.explore.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class AvailableOrderFormatTest {

    private val now = Instant.parse("2026-11-13T15:00:00Z")

    @Test
    fun `la distancia se muestra en km con un decimal como maximo`() {
        assertEquals("a 1.2 km", formatDistance(1.2))
        assertEquals("a 5 km", formatDistance(5.0))
        assertNull(formatDistance(null))
    }

    @Test
    fun `el tiempo desde la publicacion va en minutos, horas o dias`() {
        assertEquals("ahora", formatTimeAgo(now.minusSeconds(20), now))
        assertEquals("hace 20 min", formatTimeAgo(now.minusSeconds(20 * 60), now))
        assertEquals("hace 2 h", formatTimeAgo(now.minusSeconds(2 * 3600 + 300), now))
        assertEquals("ayer", formatTimeAgo(now.minusSeconds(30 * 3600), now))
        assertEquals("hace 3 días", formatTimeAgo(now.minusSeconds(3 * 86_400 + 60), now))
    }

    @Test
    fun `peso e items se muestran como en mis pedidos`() {
        assertEquals("14.5 kg", formatAvailableWeight(14.5))
        assertEquals("22 kg", formatAvailableWeight(22.0))
        assertEquals("1 ítem", formatAvailableItems(1))
        assertEquals("4 ítems", formatAvailableItems(4))
    }
}
