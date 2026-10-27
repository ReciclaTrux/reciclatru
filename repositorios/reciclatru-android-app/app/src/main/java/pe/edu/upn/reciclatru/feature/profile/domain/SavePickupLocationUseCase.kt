package pe.edu.upn.reciclatru.feature.profile.domain

import javax.inject.Inject

/**
 * Guarda el punto de recojo. Las reglas de negocio (una ubicación vigente por vecino,
 * quién puede tener ubicación) las valida la API; acá solo se protege lo obvio para no
 * enviar una petición vacía.
 */
class SavePickupLocationUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(location: PickupLocation): PickupLocation {
        require(location.address.isNotBlank()) { "La dirección no puede estar vacía." }
        require(location.district.isNotBlank()) { "El distrito no puede estar vacío." }
        return profileRepository.savePickupLocation(location)
    }
}
