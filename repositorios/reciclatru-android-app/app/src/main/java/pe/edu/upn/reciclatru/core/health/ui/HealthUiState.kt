package pe.edu.upn.reciclatru.core.health.ui

data class HealthUiState(
    val checking: Boolean = true,
    val connected: Boolean = false,
    val error: String? = null,
)
