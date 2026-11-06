package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.edu.upn.reciclatru.core.network.NetworkException
import pe.edu.upn.reciclatru.core.network.toUserMessage
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.profile.domain.GetProfileUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.UpdateProfileUseCase
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfile
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() {
        load()
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value.take(MaxNameLength), fullNameError = null, saveError = null) }
    }

    fun onPhoneChange(value: String) {
        val digits = value.filter(Char::isDigit).take(PhoneLength)
        _uiState.update { it.copy(phone = digits, phoneError = null, saveError = null) }
    }

    fun onReferenceChange(value: String) {
        _uiState.update { it.copy(reference = value.take(MaxReferenceLength), saveError = null) }
    }

    fun onReturnFromPickupPoint() {
        val current = _uiState.value
        if (current.loading || current.loadError != null) return
        viewModelScope.launch {
            try {
                val profile = getProfile()
                _uiState.update { it.copy(pickupAddress = profile.pickupAddress) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                return@launch
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onSave() {
        val current = _uiState.value
        if (current.saving || current.loading) return

        val fullName = current.fullName.trim()
        val fullNameError = if (fullName.length < MinNameLength) FullNameRequiredMessage else null
        val phoneError = if (!PhonePattern.matches(current.phone)) PhoneInvalidMessage else null
        if (fullNameError != null || phoneError != null) {
            _uiState.update { it.copy(fullNameError = fullNameError, phoneError = phoneError, saveError = null) }
            return
        }

        val reference = if (current.role == UserRole.Neighbor) current.reference else null
        _uiState.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            try {
                val profile = updateProfile(fullName = fullName, phone = current.phone, reference = reference)
                _uiState.update {
                    it.withProfile(profile).copy(saving = false, savedOnce = true, message = SavedMessage)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(saving = false, saveError = error.toSaveMessage()) }
            }
        }
    }

    private fun load() {
        _uiState.update { it.copy(loading = true, loadError = null) }
        viewModelScope.launch {
            try {
                val profile = getProfile()
                _uiState.update { it.withProfile(profile).copy(loading = false) }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update { it.copy(loading = false, loadError = error.toUserMessage()) }
            }
        }
    }

    private fun ProfileUiState.withProfile(profile: UserProfile): ProfileUiState = copy(
        email = profile.email,
        role = profile.role,
        savedFullName = profile.fullName.orEmpty(),
        fullName = profile.fullName.orEmpty(),
        phone = profile.phone.orEmpty(),
        reference = profile.reference.orEmpty(),
        pickupAddress = profile.pickupAddress,
    )

    private fun Exception.toSaveMessage(): String =
        if ((this as? NetworkException)?.statusCode == BadRequestCode) {
            InvalidDataMessage
        } else {
            toUserMessage()
        }

    private companion object {
        const val MinNameLength = 2
        const val MaxNameLength = 80
        const val PhoneLength = 9
        const val MaxReferenceLength = 150
        const val BadRequestCode = 400
        val PhonePattern = Regex("^9\\d{8}$")
        const val FullNameRequiredMessage = "Ingresa tu nombre y apellido."
        const val PhoneInvalidMessage = "Debe tener 9 dígitos y empezar con 9"
        const val InvalidDataMessage = "Revisa tus datos e inténtalo de nuevo."
        const val SavedMessage = "Guardamos tu perfil."
    }
}
