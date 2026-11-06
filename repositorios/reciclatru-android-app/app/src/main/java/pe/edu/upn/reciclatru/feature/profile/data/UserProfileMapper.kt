package pe.edu.upn.reciclatru.feature.profile.data

import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.feature.profile.domain.UserProfile

fun PartyProfileOutputDto.toUserProfile(): UserProfile? {
    val record = record ?: return null
    val role = record.role?.let { value -> UserRole.entries.firstOrNull { it.apiValue == value } }
        ?: return null
    return UserProfile(
        email = record.email.orEmpty(),
        role = role,
        fullName = record.fullName,
        phone = record.phone,
        reference = record.reference,
        pickupAddress = record.pickupPoint?.toPickupAddress(),
        profileComplete = record.profileComplete ?: false,
    )
}

private fun ProfilePickupPointDto.toPickupAddress(): PickupAddress? {
    if (latitude == null || longitude == null) return null
    return PickupAddress(address = address.orEmpty(), district = district.orEmpty())
}
