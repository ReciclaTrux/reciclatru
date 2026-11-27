package pe.edu.upn.reciclatru.viewmodel.explore

import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import pe.edu.upn.reciclatru.model.explore.MyAcceptance

data class AcceptancesTabState(
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val acceptances: List<MyAcceptance> = emptyList(),
    val nextCursor: String? = null,
    val error: String? = null,
)

data class MyAcceptancesUiState(
    val selectedTab: AcceptancesTab = AcceptancesTab.Active,
    val active: AcceptancesTabState = AcceptancesTabState(),
    val history: AcceptancesTabState = AcceptancesTabState(),
    val message: String? = null,
) {
    val current: AcceptancesTabState
        get() = tab(selectedTab)

    fun tab(tab: AcceptancesTab): AcceptancesTabState = when (tab) {
        AcceptancesTab.Active -> active
        AcceptancesTab.History -> history
    }
}
