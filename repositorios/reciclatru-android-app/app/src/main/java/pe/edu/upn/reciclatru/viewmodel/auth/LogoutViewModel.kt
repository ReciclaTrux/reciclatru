package pe.edu.upn.reciclatru.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.domain.usecase.auth.LogoutUseCase
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val logout: LogoutUseCase,
) : ViewModel() {

    fun onLogout() {
        viewModelScope.launch { logout() }
    }
}
