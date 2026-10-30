package pe.edu.upn.reciclatru.core.health.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.health.domain.CheckBackendHealthUseCase
import pe.edu.upn.reciclatru.core.network.toUserMessage
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class HealthViewModel @Inject constructor(
    private val checkBackendHealth: CheckBackendHealthUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        check()
    }

    fun onRetry() {
        check()
    }

    private fun check() {
        _uiState.update { it.copy(checking = true, error = null) }
        viewModelScope.launch {
            try {
                val up = checkBackendHealth()
                _uiState.update {
                    it.copy(
                        checking = false,
                        connected = up,
                        error = if (up) null else ServerUnavailableMessage,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        checking = false,
                        connected = false,
                        error = error.toUserMessage(fallback = ServerUnavailableMessage),
                    )
                }
            }
        }
    }

    private companion object {
        const val ServerUnavailableMessage =
            "Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento."
    }
}
