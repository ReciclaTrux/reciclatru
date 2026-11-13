package pe.edu.upn.reciclatru.feature.orders.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
import java.time.Instant

class OrderMapperTest {

    private val gson = Gson()

    @Test
    fun `el cuerpo del pedido sigue el contrato`() {
        val request = listOf(OrderItem("Botellas de plástico PET", 4.5), OrderItem("Cartón corrugado", 8.0))
            .toInitiateRequest()

        assertEquals(
            """{"items":[{"description":"Botellas de plástico PET","weightKg":4.5},{"description":"Cartón corrugado","weightKg":8.0}]}""",
            gson.toJson(request),
        )
    }

    @Test
    fun `la respuesta 201 se convierte en un pedido pendiente`() {
        val json = """
            {
              "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
              "servicingOrderInstanceRecord": {
                "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
                "status": "PENDING",
                "items": [
                  { "description": "Botellas de plástico PET", "weightKg": 4.5 },
                  { "description": "Cartón corrugado", "weightKg": 8.0 }
                ],
                "totalWeightKg": 12.5,
                "photos": []
              }
            }
        """.trimIndent()

        val order = gson.fromJson(json, ServicingOrderOutputDto::class.java).toOrder()

        assertEquals("0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36", order?.id)
        assertEquals(OrderStatus.Pending, order?.status)
        assertEquals(2, order?.items?.size)
        assertEquals(12.5, order?.totalWeightKg ?: 0.0, 0.0)
    }

    @Test
    fun `una respuesta sin registro no se convierte`() {
        assertNull(gson.fromJson("{}", ServicingOrderOutputDto::class.java).toOrder())
    }

    @Test
    fun `la lista de mis pedidos se convierte con su estado y fecha`() {
        val json = """
            {
              "items": [
                {
                  "id": "0f8a3c52",
                  "status": "PENDING",
                  "itemCount": 3,
                  "totalWeightKg": 13.7,
                  "acceptanceCount": 2,
                  "chosenRecyclerName": null,
                  "createdAt": "2026-10-26T14:05:00Z"
                },
                {
                  "id": "5b2e9d41",
                  "status": "CONFIRMED",
                  "itemCount": 2,
                  "totalWeightKg": 11.5,
                  "acceptanceCount": null,
                  "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
                  "createdAt": "2026-10-23T14:10:00Z"
                }
              ],
              "nextCursor": "abc"
            }
        """.trimIndent()

        val page = gson.fromJson(json, ServicingOrderListOutputDto::class.java).toOrdersPage()

        assertEquals("abc", page.nextCursor)
        assertEquals(2, page.items.size)
        assertEquals(OrderStatus.Pending, page.items[0].status)
        assertEquals(2, page.items[0].acceptanceCount)
        assertEquals(Instant.parse("2026-10-26T14:05:00Z"), page.items[0].createdAt)
        assertEquals("Jorge Luis Mendoza Ruiz", page.items[1].chosenRecyclerName)
    }

    @Test
    fun `un pedido confirmado trae el contacto del reciclador y permite cancelar`() {
        val json = """
            {
              "crReferenceId": "5b2e9d41",
              "servicingOrderInstanceRecord": {
                "id": "5b2e9d41",
                "status": "CONFIRMED",
                "items": [{ "description": "Cartón", "weightKg": 9.0 }],
                "totalWeightKg": 9.0,
                "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
                "counterpart": { "fullName": "Jorge Luis Mendoza Ruiz", "phone": "944123456" },
                "availableActions": ["MARK_COLLECTED", "CANCEL"]
              }
            }
        """.trimIndent()

        val order = gson.fromJson(json, ServicingOrderOutputDto::class.java).toOrder()

        assertEquals("944123456", order?.counterpart?.phone)
        assertEquals(true, order?.canCancel)
    }

    @Test
    fun `un pedido cancelado ya no permite cancelar`() {
        val json = """
            {
              "servicingOrderInstanceRecord": {
                "id": "92d6b0f3",
                "status": "CANCELLED",
                "items": [],
                "totalWeightKg": 3.0,
                "availableActions": []
              }
            }
        """.trimIndent()

        val order = gson.fromJson(json, ServicingOrderOutputDto::class.java).toOrder()

        assertEquals(OrderStatus.Cancelled, order?.status)
        assertEquals(false, order?.canCancel)
    }

    @Test
    fun `las pestanas viajan como ACTIVE e HISTORY`() {
        assertEquals("ACTIVE", OrdersTab.Active.toQuery())
        assertEquals("HISTORY", OrdersTab.History.toQuery())
    }
}
