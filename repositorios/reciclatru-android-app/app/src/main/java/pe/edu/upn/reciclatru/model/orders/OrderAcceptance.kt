package pe.edu.upn.reciclatru.model.orders

import java.time.Instant

enum class AcceptanceStatus { Accepted, Chosen, Discarded }

/** Reciclador que aceptó un pedido (HU-13). La API las devuelve de la primera a la última. */
data class OrderAcceptance(
    val id: String,
    val status: AcceptanceStatus,
    val fullName: String,
    val phone: String,
    val acceptedAt: Instant?,
)
