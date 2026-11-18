package pe.edu.upn.reciclatru.viewmodel.explore

import pe.edu.upn.reciclatru.model.explore.AvailableOrder

enum class LocationNotice { PermissionDenied, PermissionDeniedPermanently, Unavailable }

data class AvailableOrdersUiState(
    val showLocationPrompt: Boolean = false,
    val locationNotice: LocationNotice? = null,
    val loaded: Boolean = false,
    val loading: Boolean = false,
    val refreshing: Boolean = false,
    val loadingMore: Boolean = false,
    val orders: List<AvailableOrder> = emptyList(),
    val nextCursor: String? = null,
    val sortedByDistance: Boolean = false,
    val loadError: String? = null,
    val message: String? = null,
)
