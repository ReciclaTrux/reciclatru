package pe.edu.upn.reciclatru.feature.orders.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.edu.upn.reciclatru.feature.orders.domain.OrderItem
import pe.edu.upn.reciclatru.feature.orders.domain.OrderStatus

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
}
