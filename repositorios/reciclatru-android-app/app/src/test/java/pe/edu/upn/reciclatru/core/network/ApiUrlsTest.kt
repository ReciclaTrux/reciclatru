package pe.edu.upn.reciclatru.core.network

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiUrlsTest {

    private val baseUrl = "https://reciclatru.onrender.com/api/v1/"

    @Test
    fun `una ruta de la API se completa con el servidor`() {
        assertEquals(
            "https://reciclatru.onrender.com/api/v1/servicing-order/1/photo/2/retrieval",
            "/api/v1/servicing-order/1/photo/2/retrieval".toAbsoluteApiUrl(baseUrl),
        )
    }

    @Test
    fun `una direccion completa no cambia`() {
        assertEquals("https://otro.example.com/foto.jpg", "https://otro.example.com/foto.jpg".toAbsoluteApiUrl(baseUrl))
    }
}
