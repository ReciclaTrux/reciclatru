package pe.edu.upn.reciclatru.data.remote.mapper.explore

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upn.reciclatru.data.remote.dto.explore.MyAcceptanceListOutputDto
import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import java.time.Instant

class MyAcceptanceMapperTest {

    private val gson = Gson()

    @Test
    fun `lee las aceptaciones con su etiqueta y el cursor`() {
        val json = """
            { "items": [
                { "orderId": "o1", "acceptanceLabel": "CHOSEN", "zone": "Huanchaco",
                  "totalWeightKg": 14.5, "acceptedAt": "2026-11-20T14:15:00Z" },
                { "orderId": "o2", "acceptanceLabel": "AWAITING_CONFIRMATION", "zone": "Trujillo",
                  "totalWeightKg": 4.0, "acceptedAt": "2026-11-19T10:00:00Z" }
              ],
              "nextCursor": "c2" }
        """.trimIndent()

        val page = gson.fromJson(json, MyAcceptanceListOutputDto::class.java).toMyAcceptancesPage()

        assertEquals(listOf("o1", "o2"), page.items.map { it.orderId })
        assertEquals(AcceptanceLabel.Chosen, page.items[0].label)
        assertEquals(AcceptanceLabel.AwaitingConfirmation, page.items[1].label)
        assertEquals("Huanchaco", page.items[0].zone)
        assertEquals(14.5, page.items[0].totalWeightKg, 0.0)
        assertEquals(Instant.parse("2026-11-20T14:15:00Z"), page.items[0].acceptedAt)
        assertEquals("c2", page.nextCursor)
    }

    @Test
    fun `las etiquetas del historial se leen y las desconocidas se descartan`() {
        val json = """
            { "items": [
                { "orderId": "h1", "acceptanceLabel": "NOT_CHOSEN" },
                { "orderId": "h2", "acceptanceLabel": "CANCELLED_BY_NEIGHBOR" },
                { "orderId": "h3", "acceptanceLabel": "COLLECTED" },
                { "orderId": "h4", "acceptanceLabel": "OTRO" },
                { "acceptanceLabel": "COLLECTED" }
              ],
              "nextCursor": null }
        """.trimIndent()

        val page = gson.fromJson(json, MyAcceptanceListOutputDto::class.java).toMyAcceptancesPage()

        assertEquals(
            listOf(AcceptanceLabel.NotChosen, AcceptanceLabel.CancelledByNeighbor, AcceptanceLabel.Collected),
            page.items.map { it.label },
        )
        assertEquals(0.0, page.items[0].totalWeightKg, 0.0)
        assertNull(page.items[0].acceptedAt)
        assertNull(page.nextCursor)
    }

    @Test
    fun `la pestana se envia como ACTIVE o HISTORY`() {
        assertEquals("ACTIVE", AcceptancesTab.Active.toQuery())
        assertEquals("HISTORY", AcceptancesTab.History.toQuery())
    }
}
