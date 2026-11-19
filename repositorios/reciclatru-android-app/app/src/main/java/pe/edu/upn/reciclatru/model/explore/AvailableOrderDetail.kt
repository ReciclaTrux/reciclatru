package pe.edu.upn.reciclatru.model.explore

import pe.edu.upn.reciclatru.model.orders.AcceptanceStatus
import pe.edu.upn.reciclatru.model.orders.OrderContact
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.model.orders.OrderPhoto
import pe.edu.upn.reciclatru.model.orders.OrderStatus
import java.time.Instant

/**
 * Detalle de un pedido visto por un reciclador (HU-11, HU-12). Antes de aceptar no trae el
 * contacto del vecino; el backend lo envía solo cuando ya existe una aceptación.
 */
data class AvailableOrderDetail(
    val id: String,
    val status: OrderStatus,
    val zone: String,
    val items: List<OrderItem>,
    val totalWeightKg: Double,
    val distanceKm: Double?,
    val createdAt: Instant?,
    val acceptanceCount: Int,
    val acceptanceStatus: AcceptanceStatus?,
    val photos: List<OrderPhoto>,
    val neighbor: OrderContact?,
) {
    val alreadyAccepted: Boolean get() = acceptanceStatus != null
}
