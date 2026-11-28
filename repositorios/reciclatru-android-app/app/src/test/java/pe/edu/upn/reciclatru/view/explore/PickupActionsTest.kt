package pe.edu.upn.reciclatru.view.explore

import org.junit.Assert.assertEquals
import org.junit.Test
import pe.edu.upn.reciclatru.model.explore.PickupPoint

class PickupActionsTest {

    @Test
    fun `como llegar en el navegador usa google maps sin api key`() {
        val pickup = PickupPoint("Av. Larco 1250", null, -8.1342, -79.0267)

        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=-8.1342,-79.0267",
            pickup.webDirectionsUrl(),
        )
    }

    @Test
    fun `la direccion copiada lleva calle y distrito o las coordenadas`() {
        assertEquals(
            "Av. Larco 1250, Víctor Larco Herrera",
            PickupPoint("Av. Larco 1250", null, -8.1, -79.0).copyText("Víctor Larco Herrera"),
        )
        assertEquals("Huanchaco", PickupPoint(null, "Puerta negra", -8.1, -79.0).copyText("Huanchaco"))
        assertEquals("-8.1, -79.0", PickupPoint(null, null, -8.1, -79.0).copyText(""))
    }
}
