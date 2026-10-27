package pe.edu.upn.reciclatru.core.session

/** Rol del usuario según el `enum` de la API. Solo sirve para la UX y la navegación. */
enum class UserRole(val apiValue: String) {
    Neighbor("NEIGHBOR"),
    Recycler("RECYCLER"),
}

/**
 * Sesión activa. El token se guarda en DataStore y viaja en cada petición; la API valida
 * los permisos y el rol, no la app.
 */
data class Session(
    val userId: String,
    val token: String,
    val role: UserRole,
)
