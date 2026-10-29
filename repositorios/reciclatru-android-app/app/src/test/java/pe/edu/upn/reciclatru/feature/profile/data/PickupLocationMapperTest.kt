package pe.edu.upn.reciclatru.feature.profile.data

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.edu.upn.reciclatru.feature.profile.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.profile.domain.PickupLocation

class PickupLocationMapperTest {

    @Test
    fun `el cuerpo usa los nombres snake_case del modelo de datos`() {
        val location = PickupLocation(
            point = GeoPoint(latitude = -8.1116, longitude = -79.0287),
            address = "Av. Larco 1250",
            district = "Víctor Larco Herrera",
            referenceNote = "Portón verde",
        )

        val json = Gson().toJson(location.toRequestDto())

        assertTrue(json.contains("\"reference_note\":\"Portón verde\""))
        assertTrue(json.contains("\"latitude\":-8.1116"))
        assertTrue(json.contains("\"district\":\"Víctor Larco Herrera\""))
    }

    @Test
    fun `si la respuesta omite campos se conserva lo enviado`() {
        val sent = PickupLocation(
            point = GeoPoint(latitude = -8.1116, longitude = -79.0287),
            address = "Av. Larco 1250",
            district = "Víctor Larco Herrera",
        )

        val mapped = PickupLocationDto(id = "location-1").toDomain(fallback = sent)

        assertEquals("location-1", mapped.id)
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

        val mapped = PickupLocationDto(
            id = "location-2",
            latitude = -8.1200,
            longitude = -79.0300,
            address = "Av. Larco 1260",
            district = "Víctor Larco",
        ).toDomain(fallback = sent)

        assertEquals("location-2", mapped.id)
        assertEquals(-8.1200, mapped.point.latitude, 0.0)
        assertEquals("Av. Larco 1260", mapped.address)
        assertEquals("Víctor Larco", mapped.district)
    }
}
