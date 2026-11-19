package pe.edu.upn.reciclatru.data.remote.mapper.explore

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.reciclatru.data.remote.dto.orders.ServicingOrderOutputDto
import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderStatus

class AvailableOrderDetailMapperTest {

    private val gson = Gson()

    @Test
    fun `el detalle antes de aceptar no trae contacto del vecino`() {
        val json = """
            { "crReferenceId": "o1",
              "servicingOrderInstanceRecord": {
                "id": "o1", "status": "PENDING",
                "items": [ { "description": "Cartón", "weightKg": 10.0 } ],
                "totalWeightKg": 10.0, "photos": [],
                "zone": "Huanchaco", "approximateDistanceKm": 1.2, "acceptanceCount": 2,
                "acceptanceStatus": null, "counterpart": null,
                "createdAt": "2026-10-26T14:05:00Z"
              } }
        """.trimIndent()

        val detail = gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()

        assertEquals("Huanchaco", detail?.zone)
        assertEquals(1.2, detail?.distanceKm ?: 0.0, 0.0)
        assertEquals(2, detail?.acceptanceCount)
        assertEquals(OrderStatus.Pending, detail?.status)
        assertFalse(detail?.alreadyAccepted == true)
        assertNull(detail?.neighbor)
    }

    @Test
    fun `tras aceptar trae el contacto y el estado de la aceptacion`() {
        val json = """
            { "servicingOrderInstanceRecord": {
                "id": "o1", "status": "PENDING", "items": [], "totalWeightKg": 0,
                "zone": "Huanchaco", "alreadyAccepted": true, "acceptanceStatus": "ACCEPTED",
                "counterpart": { "fullName": "Rosa Elena Carranza Vega", "phone": "987654321" }
              } }
        """.trimIndent()

        val detail = gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()

        assertTrue(detail?.alreadyAccepted == true)
        assertEquals(AcceptanceStatus.Accepted, detail?.acceptanceStatus)
        assertEquals("987654321", detail?.neighbor?.phone)
    }

    @Test
    fun `alreadyAccepted sin estado se trata como aceptada`() {
        val json = """
            { "servicingOrderInstanceRecord": { "id": "o1", "status": "PENDING", "alreadyAccepted": true } }
        """.trimIndent()

        val detail = gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()

        assertEquals(AcceptanceStatus.Accepted, detail?.acceptanceStatus)
    }

    @Test
    fun `una respuesta sin registro no se convierte`() {
        assertNull(gson.fromJson("{}", ServicingOrderOutputDto::class.java).toAvailableOrderDetail())
    }
}
