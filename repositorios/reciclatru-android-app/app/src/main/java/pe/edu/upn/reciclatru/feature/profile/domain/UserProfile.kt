package pe.edu.upn.reciclatru.feature.profile.domain

import pe.edu.upn.reciclatru.core.session.UserRole

data class UserProfile(
    val email: String,
    val role: UserRole,
    val fullName: String?,
    val phone: String?,
    val reference: String?,
    val pickupAddress: PickupAddress?,
    val profileComplete: Boolean,
)
