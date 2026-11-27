package pe.edu.upn.reciclatru.viewmodel.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.data.remote.toUserMessage
import pe.edu.upn.reciclatru.domain.usecase.explore.GetMyAcceptancesUseCase
import pe.edu.upn.reciclatru.model.explore.AcceptancesTab
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MyAcceptancesViewModel @Inject constructor(
    private val getMyAcceptances: GetMyAcceptancesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyAcceptancesUiState())
    val uiState: StateFlow<MyAcceptancesUiState> = _uiState.asStateFlow()

    init {
        load(AcceptancesTab.Active)
    }

    fun onTabSelected(tab: AcceptancesTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        if (_uiState.value.tab(tab).loaded) refreshSilently(tab) else load(tab)
    }

    fun onRetry() {
        load(_uiState.value.selectedTab)
    }

    fun onRefresh() {
        val tab = _uiState.value.selectedTab
        val current = _uiState.value.tab(tab)
        if (current.loading || current.refreshing) return
        updateTab(tab) { it.copy(refreshing = true) }
        viewModelScope.launch {
            try {
                val page = getMyAcceptances(tab)
                updateTab(tab) {
                    it.copy(
                        loaded = true,
                        refreshing = false,
                        acceptances = page.items,
                        nextCursor = page.nextCursor,
                        error = null,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                updateTab(tab) { it.copy(refreshing = false) }
                _uiState.update { it.copy(message = error.toUserMessage()) }
            }
        }
    }

    fun onResume() {
        refreshSilently(_uiState.value.selectedTab)
    }

    fun onLoadMore() {
        val tab = _uiState.value.selectedTab
        val current = _uiState.value.tab(tab)
        val cursor = current.nextCursor ?: return
        if (current.loading || current.refreshing || current.loadingMore) return
        updateTab(tab) { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val page = getMyAcceptances(tab, cursor)
                updateTab(tab) {
                    it.copy(
                        loadingMore = false,
                        acceptances = it.acceptances + page.items,
                        nextCursor = page.nextCursor,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                updateTab(tab) { it.copy(loadingMore = false) }
                _uiState.update { it.copy(message = error.toUserMessage()) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun refreshSilently(tab: AcceptancesTab) {
        val current = _uiState.value.tab(tab)
        if (!current.loaded || current.loading || current.refreshing) return
        viewModelScope.launch {
            try {
                val page = getMyAcceptances(tab)
                updateTab(tab) { it.copy(acceptances = page.items, nextCursor = page.nextCursor) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            }
        }
    }

    private fun load(tab: AcceptancesTab) {
        if (_uiState.value.tab(tab).loading) return
        updateTab(tab) { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val page = getMyAcceptances(tab)
                updateTab(tab) {
                    it.copy(loaded = true, loading = false, acceptances = page.items, nextCursor = page.nextCursor)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                updateTab(tab) { it.copy(loading = false, error = error.toUserMessage()) }
            }
        }
    }

    private fun updateTab(tab: AcceptancesTab, transform: (AcceptancesTabState) -> AcceptancesTabState) {
        _uiState.update { state ->
            when (tab) {
                AcceptancesTab.Active -> state.copy(active = transform(state.active))
                AcceptancesTab.History -> state.copy(history = transform(state.history))
            }
        }
    }
}
