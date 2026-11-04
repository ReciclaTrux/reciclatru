package pe.edu.upn.reciclatru.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.feature.auth.domain.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val logout: LogoutUseCase,
) : ViewModel() {

    fun onLogout() {
        viewModelScope.launch { logout() }
    }
}
