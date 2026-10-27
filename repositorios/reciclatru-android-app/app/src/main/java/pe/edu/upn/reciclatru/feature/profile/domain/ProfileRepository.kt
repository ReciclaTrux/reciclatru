package pe.edu.upn.reciclatru.feature.profile.domain

/**
 * Datos del vecino que la API expone. La app nunca inventa ni completa permisos: si un
 * campo no llega, no se muestra.
 */
interface ProfileRepository {
    /**
     * Guarda el punto de recojo vigente. La API crea una fila nueva y deja la anterior
     * sin `is_current` (RN18); devuelve la ubicación ya persistida.
     */
    suspend fun savePickupLocation(location: PickupLocation): PickupLocation
}
