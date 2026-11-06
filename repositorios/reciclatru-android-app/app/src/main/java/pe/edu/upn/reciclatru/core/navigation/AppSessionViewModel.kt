package pe.edu.upn.reciclatru.core.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.session.SessionStore
import pe.edu.upn.reciclatru.core.session.SignOutReason
import javax.inject.Inject

@HiltViewModel
class AppSessionViewModel @Inject constructor(
    private val sessionStore: SessionStore,
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    val signedOut: SharedFlow<SignOutReason> = sessionStore.signedOut

    init {
        viewModelScope.launch {
            _startDestination.value = if (sessionStore.loadSession() != null) {
                AppRoutes.Home
            } else {
                AppRoutes.login()
            }
        }
    }
}
