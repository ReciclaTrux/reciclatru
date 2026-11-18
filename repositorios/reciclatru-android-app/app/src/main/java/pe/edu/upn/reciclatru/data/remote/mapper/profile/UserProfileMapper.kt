package pe.edu.upn.reciclatru.data.remote.mapper.profile

import pe.edu.upn.reciclatru.data.remote.dto.profile.PartyProfileOutputDto
import pe.edu.upn.reciclatru.data.remote.dto.profile.ProfilePickupPointDto
import pe.edu.upn.reciclatru.model.profile.PickupAddress
import pe.edu.upn.reciclatru.model.profile.UserProfile
import pe.edu.upn.reciclatru.model.session.UserRole

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
