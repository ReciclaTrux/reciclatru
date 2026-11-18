package pe.edu.upn.reciclatru.viewmodel.profile

import pe.edu.upn.reciclatru.model.profile.PickupAddress
import pe.edu.upn.reciclatru.model.session.UserRole

data class ProfileUiState(
    val loading: Boolean = true,
    val loadError: String? = null,
    val email: String = "",
    val role: UserRole? = null,
    val savedFullName: String = "",
    val fullName: String = "",
    val phone: String = "",
    val reference: String = "",
    val pickupAddress: PickupAddress? = null,
    val fullNameError: String? = null,
    val phoneError: String? = null,
    val saveError: String? = null,
    val saving: Boolean = false,
    val savedOnce: Boolean = false,
    val message: String? = null,
)
