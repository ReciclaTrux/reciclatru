package pe.edu.upn.reciclatru.data.remote.mapper.explore

import pe.edu.upn.reciclatru.data.remote.dto.explore.MyAcceptanceDto
import pe.edu.upn.reciclatru.data.remote.dto.explore.MyAcceptanceListOutputDto
import pe.edu.upn.reciclatru.data.remote.mapper.orders.toInstantOrNull
import pe.edu.upn.reciclatru.model.explore.AcceptanceLabel
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptance
import pe.edu.upn.reciclatru.model.explore.MyAcceptancesPage

fun AcceptancesTab.toQuery(): String = when (this) {
    AcceptancesTab.Active -> "ACTIVE"
    AcceptancesTab.History -> "HISTORY"
}

fun MyAcceptanceListOutputDto.toMyAcceptancesPage(): MyAcceptancesPage = MyAcceptancesPage(
    items = items.orEmpty().mapNotNull { it.toMyAcceptance() },
    nextCursor = nextCursor,
)

private fun MyAcceptanceDto.toMyAcceptance(): MyAcceptance? {
    val orderId = orderId ?: return null
    val label = acceptanceLabel.toAcceptanceLabel() ?: return null
    return MyAcceptance(
        orderId = orderId,
        label = label,
        zone = zone.orEmpty(),
        totalWeightKg = totalWeightKg ?: 0.0,
        acceptedAt = acceptedAt.toInstantOrNull(),
    )
}

private fun String?.toAcceptanceLabel(): AcceptanceLabel? = when (this) {
    "AWAITING_CONFIRMATION" -> AcceptanceLabel.AwaitingConfirmation
    "CHOSEN" -> AcceptanceLabel.Chosen
    "NOT_CHOSEN" -> AcceptanceLabel.NotChosen
    "CANCELLED_BY_NEIGHBOR" -> AcceptanceLabel.CancelledByNeighbor
    "COLLECTED" -> AcceptanceLabel.Collected
    else -> null
}
