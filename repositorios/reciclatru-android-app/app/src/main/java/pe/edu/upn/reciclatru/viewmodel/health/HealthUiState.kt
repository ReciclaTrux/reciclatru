package pe.edu.upn.reciclatru.viewmodel.health

data class HealthUiState(
    val checking: Boolean = true,
    val connected: Boolean = false,
    val error: String? = null,
)
