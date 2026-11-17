package pe.edu.upn.reciclatru.feature.orders.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.orders.domain.GetMyOrdersUseCase
import pe.edu.upn.reciclatru.feature.orders.domain.OrdersTab
import pe.edu.upn.reciclatru.feature.orders.domain.PendingPhotosStore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class MyOrdersViewModel @Inject constructor(
    private val getMyOrders: GetMyOrdersUseCase,
    private val pendingPhotos: PendingPhotosStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyOrdersUiState())
    val uiState: StateFlow<MyOrdersUiState> = _uiState.asStateFlow()

    init {
        load(OrdersTab.Active)
    }

    fun onTabSelected(tab: OrdersTab) {
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
                val page = getMyOrders(tab)
                updateTab(tab) {
                    it.copy(loaded = true, refreshing = false, orders = page.items, nextCursor = page.nextCursor, error = null)
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

    private fun refreshSilently(tab: OrdersTab) {
        val current = _uiState.value.tab(tab)
        if (!current.loaded || current.loading || current.refreshing) return
        viewModelScope.launch {
            try {
                val page = getMyOrders(tab)
                updateTab(tab) { it.copy(orders = page.items, nextCursor = page.nextCursor) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            }
        }
    }

    fun onLoadMore() {
        val tab = _uiState.value.selectedTab
        val current = _uiState.value.tab(tab)
        val cursor = current.nextCursor ?: return
        if (current.loading || current.refreshing || current.loadingMore) return
        updateTab(tab) { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val page = getMyOrders(tab, cursor)
                updateTab(tab) {
                    it.copy(loadingMore = false, orders = it.orders + page.items, nextCursor = page.nextCursor)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                updateTab(tab) { it.copy(loadingMore = false) }
                _uiState.update { it.copy(message = error.toUserMessage()) }
            }
        }
    }

    fun onOrderPublished(orderId: String) {
        val failedPhotos = pendingPhotos.get(orderId).size
        _uiState.update {
            it.copy(
                selectedTab = OrdersTab.Active,
                message = when (failedPhotos) {
                    0 -> PublishedMessage
                    1 -> "Pedido publicado, pero no se subió 1 foto"
                    else -> "Pedido publicado, pero no se subieron $failedPhotos fotos"
                },
                messageOrderId = orderId.ifBlank { null },
            )
        }
        load(OrdersTab.Active)
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null, messageOrderId = null) }
    }

    private fun load(tab: OrdersTab) {
        if (_uiState.value.tab(tab).loading) return
        updateTab(tab) { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                val page = getMyOrders(tab)
                updateTab(tab) {
                    it.copy(loaded = true, loading = false, orders = page.items, nextCursor = page.nextCursor)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                updateTab(tab) { it.copy(loading = false, error = error.toUserMessage()) }
            }
        }
    }

    private fun updateTab(tab: OrdersTab, transform: (OrdersTabState) -> OrdersTabState) {
        _uiState.update { state ->
            when (tab) {
                OrdersTab.Active -> state.copy(active = transform(state.active))
                OrdersTab.History -> state.copy(history = transform(state.history))
            }
        }
    }

    private companion object {
        const val PublishedMessage = "Pedido publicado"
    }
}
