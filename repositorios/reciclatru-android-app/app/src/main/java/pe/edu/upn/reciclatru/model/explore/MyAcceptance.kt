package pe.edu.upn.reciclatru.model.explore

import java.time.Instant

enum class AcceptanceLabel { AwaitingConfirmation, Chosen, NotChosen, CancelledByNeighbor, Collected }

enum class AcceptancesTab { Active, History }

/** Pedido que el reciclador aceptó (HU-14), con la etiqueta que combina aceptación y pedido. */
data class MyAcceptance(
    val orderId: String,
    val label: AcceptanceLabel,
    val zone: String,
    val totalWeightKg: Double,
    val acceptedAt: Instant?,
)

data class MyAcceptancesPage(
    val items: List<MyAcceptance>,
    val nextCursor: String?,
)
