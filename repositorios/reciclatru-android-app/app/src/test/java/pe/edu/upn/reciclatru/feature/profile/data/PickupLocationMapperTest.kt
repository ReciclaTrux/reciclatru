package pe.edu.upn.reciclatru.feature.profile.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation

class PickupLocationMapperTest {

    @Test
    fun `el cuerpo usa los campos del contrato OpenAPI`() {
        val location = PickupLocation(
            point = GeoPoint(latitude = -8.1116, longitude = -79.0287),
            address = "Av. Larco 1250",
            district = "Víctor Larco Herrera",
            referenceNote = "Portón verde",
        )

        val json = Gson().toJson(location.toRequestDto())

        assertTrue(json.contains("\"latitude\":-8.1116"))
        assertTrue(json.contains("\"longitude\":-79.0287"))
        assertTrue(json.contains("\"address\":\"Av. Larco 1250\""))
        assertTrue(json.contains("\"district\":\"Víctor Larco Herrera\""))
        assertFalse(json.contains("reference"))
    }

    @Test
    fun `si la respuesta omite el punto se conserva lo enviado`() {
        val sent = PickupLocation(
            point = GeoPoint(latitude = -8.1116, longitude = -79.0287),
            address = "Av. Larco 1250",
            district = "Víctor Larco Herrera",
        )

        val mapped = PartyProfileOutputDto(record = PartyProfileRecordDto(id = "u-1"))
            .toPickupLocation(fallback = sent)

        assertEquals(sent.point, mapped.point)
        assertEquals(sent.address, mapped.address)
        assertEquals(sent.district, mapped.district)
    }

    @Test
    fun `la respuesta de la API reemplaza los valores cuando llegan`() {
        val sent = PickupLocation(
            point = GeoPoint(latitude = -8.1116, longitude = -79.0287),
            address = "Av. Larco 1250",
            district = "Víctor Larco Herrera",
        )

        val mapped = PartyProfileOutputDto(
            record = PartyProfileRecordDto(
                id = "u-1",
                reference = "Portón verde",
                pickupPoint = ProfilePickupPointDto(
                    latitude = -8.1200,
                    longitude = -79.0300,
                    address = "Av. Larco 1260",
                    district = "Víctor Larco",
                ),
            ),
        ).toPickupLocation(fallback = sent)

        assertEquals(-8.1200, mapped.point.latitude, 0.0)
        assertEquals("Av. Larco 1260", mapped.address)
        assertEquals("Víctor Larco", mapped.district)
        assertEquals("Portón verde", mapped.referenceNote)
    }
}
