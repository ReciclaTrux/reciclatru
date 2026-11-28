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
    fun `el reciclador elegido recibe la direccion para ir a recoger`() {
        val json = """
            { "servicingOrderInstanceRecord": {
                "id": "o1", "status": "CONFIRMED", "items": [], "totalWeightKg": 8.0,
                "acceptanceStatus": "CHOSEN",
                "counterpart": { "fullName": "Ana Torres", "phone": "948120337" },
                "pickup": { "address": "Av. Larco 1250", "reference": "Puerta negra",
                            "latitude": -8.1342, "longitude": -79.0267 },
                "availableActions": ["GO_PICKUP"]
              } }
        """.trimIndent()

        val pickup = gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()?.pickup

        assertEquals("Av. Larco 1250", pickup?.address)
        assertEquals("Puerta negra", pickup?.reference)
        assertEquals(-8.1342, pickup?.latitude ?: 0.0, 0.0)
        assertEquals(-79.0267, pickup?.longitude ?: 0.0, 0.0)
    }

    @Test
    fun `sin direccion escrita ni referencia quedan vacias`() {
        val json = """
            { "servicingOrderInstanceRecord": {
                "id": "o1", "status": "CONFIRMED", "items": [], "totalWeightKg": 8.0,
                "pickup": { "address": null, "reference": " ", "latitude": -8.1, "longitude": -79.0 }
              } }
        """.trimIndent()

        val pickup = gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()?.pickup

        assertNull(pickup?.address)
        assertNull(pickup?.reference)
        assertEquals(-8.1, pickup?.latitude ?: 0.0, 0.0)
    }

    @Test
    fun `sin datos de recojo no hay direccion`() {
        val json = """
            { "servicingOrderInstanceRecord": {
                "id": "o1", "status": "CANCELLED", "items": [], "totalWeightKg": 8.0, "pickup": null
              } }
        """.trimIndent()

        assertNull(gson.fromJson(json, ServicingOrderOutputDto::class.java).toAvailableOrderDetail()?.pickup)
    }

    @Test
    fun `una respuesta sin registro no se convierte`() {
        assertNull(gson.fromJson("{}", ServicingOrderOutputDto::class.java).toAvailableOrderDetail())
    }
}
