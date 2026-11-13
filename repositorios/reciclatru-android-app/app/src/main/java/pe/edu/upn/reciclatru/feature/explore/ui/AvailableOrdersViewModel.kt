package pe.edu.upn.reciclatru.feature.explore.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.feature.explore.domain.DeviceLocationProvider
import pe.edu.upn.reciclatru.feature.explore.domain.GeoPoint
import pe.edu.upn.reciclatru.feature.explore.domain.GetAvailableOrdersUseCase
import pe.edu.upn.reciclatru.feature.explore.domain.LocationPromptStore
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class AvailableOrdersViewModel @Inject constructor(
    private val getAvailableOrders: GetAvailableOrdersUseCase,
    private val locationProvider: DeviceLocationProvider,
    private val promptStore: LocationPromptStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AvailableOrdersUiState())
    val uiState: StateFlow<AvailableOrdersUiState> = _uiState.asStateFlow()

    private var started = false
    private var permissionGranted = false
    private var permanentlyDenied = false
    private var lastLocation: GeoPoint? = null

    fun onStart(permissionGranted: Boolean) {
        if (started) return
        started = true
        this.permissionGranted = permissionGranted
        if (!permissionGranted && !promptStore.wasShown()) {
            _uiState.update { it.copy(showLocationPrompt = true) }
        } else {
            load()
        }
    }

    fun onPromptAccepted() {
        promptStore.markShown()
        _uiState.update { it.copy(showLocationPrompt = false) }
    }

    fun onPromptDismissed() {
        promptStore.markShown()
        _uiState.update { it.copy(showLocationPrompt = false) }
        load()
    }

    fun onPermissionResult(granted: Boolean, permanentlyDenied: Boolean) {
        permissionGranted = granted
        this.permanentlyDenied = permanentlyDenied
        load()
    }

    fun onResume(permissionGranted: Boolean) {
        if (!started || _uiState.value.showLocationPrompt) return
        if (permissionGranted != this.permissionGranted) {
            this.permissionGranted = permissionGranted
            load()
        } else {
            refreshSilently()
        }
    }

    fun onRetry() {
        load()
    }

    fun onRefresh() {
        val current = _uiState.value
        if (current.loading || current.refreshing) return
        _uiState.update { it.copy(refreshing = true) }
        viewModelScope.launch {
            try {
                fetchFirstPage()
                _uiState.update { it.copy(refreshing = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(refreshing = false, message = error.toUserMessage()) }
            }
        }
    }

    fun onLoadMore() {
        val current = _uiState.value
        val cursor = current.nextCursor ?: return
        if (current.loading || current.refreshing || current.loadingMore) return
        _uiState.update { it.copy(loadingMore = true) }
        viewModelScope.launch {
            try {
                val page = getAvailableOrders(lastLocation, cursor)
                _uiState.update {
                    it.copy(loadingMore = false, orders = it.orders + page.items, nextCursor = page.nextCursor)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loadingMore = false, message = error.toUserMessage()) }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun load() {
        if (_uiState.value.loading) return
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                fetchFirstPage()
                _uiState.update { it.copy(loading = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
            }
        }
    }

    private fun refreshSilently() {
        val current = _uiState.value
        if (!current.loaded || current.loading || current.refreshing) return
        viewModelScope.launch {
            try {
                fetchFirstPage()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            }
        }
    }

    private suspend fun fetchFirstPage() {
        val location = if (permissionGranted) locationProvider.currentLocation() else null
        lastLocation = location
        val page = getAvailableOrders(location)
        _uiState.update {
            it.copy(
                loaded = true,
                orders = page.items,
                nextCursor = page.nextCursor,
                sortedByDistance = page.sortedByDistance,
                locationNotice = when {
                    !permissionGranted && permanentlyDenied -> LocationNotice.PermissionDeniedPermanently
                    !permissionGranted -> LocationNotice.PermissionDenied
                    location == null -> LocationNotice.Unavailable
                    else -> null
                },
            )
        }
    }
}
