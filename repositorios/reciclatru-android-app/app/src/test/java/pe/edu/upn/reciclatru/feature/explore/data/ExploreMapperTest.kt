package pe.edu.upn.reciclatru.feature.explore.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ExploreMapperTest {

    private val gson = Gson()

    @Test
    fun `con ubicacion la lista llega ordenada por distancia`() {
        val json = """
            {
              "sortedBy": "DISTANCE",
              "items": [
                {
                  "id": "b8e2c6a4",
                  "status": "PENDING",
                  "itemCount": 2,
                  "totalWeightKg": 6.4,
                  "thumbnailUrl": null,
                  "zone": "Víctor Larco Herrera",
                  "approximateDistanceKm": 0.8,
                  "alreadyAccepted": false,
                  "createdAt": "2026-10-26T13:20:00Z"
                },
                {
                  "id": "0f8a3c52",
                  "status": "PENDING",
                  "itemCount": 3,
                  "totalWeightKg": 13.7,
                  "zone": "Huanchaco",
                  "approximateDistanceKm": 11.3,
                  "alreadyAccepted": true,
                  "createdAt": "2026-10-26T14:05:00Z"
                }
              ],
              "nextCursor": "abc"
            }
        """.trimIndent()

        val page = gson.fromJson(json, ServicingOrderAvailabilityOutputDto::class.java).toAvailableOrdersPage()

        assertTrue(page.sortedByDistance)
        assertEquals("abc", page.nextCursor)
        assertEquals("Víctor Larco Herrera", page.items[0].zone)
        assertEquals(0.8, page.items[0].distanceKm ?: 0.0, 0.0)
        assertEquals(Instant.parse("2026-10-26T13:20:00Z"), page.items[0].createdAt)
        assertTrue(page.items[1].alreadyAccepted)
    }

    @Test
    fun `sin ubicacion la lista llega por fecha y sin distancia`() {
        val json = """
            {
              "sortedBy": "DATE",
              "items": [
                { "id": "0f8a3c52", "status": "PENDING", "itemCount": 3, "totalWeightKg": 13.7, "zone": "Huanchaco", "approximateDistanceKm": null }
              ],
              "nextCursor": null
            }
        """.trimIndent()

        val page = gson.fromJson(json, ServicingOrderAvailabilityOutputDto::class.java).toAvailableOrdersPage()

        assertFalse(page.sortedByDistance)
        assertNull(page.items[0].distanceKm)
        assertNull(page.nextCursor)
    }

    @Test
    fun `solo se muestran pedidos pendientes`() {
        val json = """
            {
              "sortedBy": "DATE",
              "items": [
                { "id": "a", "status": "PENDING", "itemCount": 1, "totalWeightKg": 1.0, "zone": "Trujillo" },
                { "id": "b", "status": "CONFIRMED", "itemCount": 1, "totalWeightKg": 1.0, "zone": "Trujillo" }
              ]
            }
        """.trimIndent()

        val page = gson.fromJson(json, ServicingOrderAvailabilityOutputDto::class.java).toAvailableOrdersPage()

        assertEquals(listOf("a"), page.items.map { it.id })
    }
}
