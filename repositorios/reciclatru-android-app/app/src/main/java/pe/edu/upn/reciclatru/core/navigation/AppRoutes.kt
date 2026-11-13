package pe.edu.upn.reciclatru.core.navigation

/**
 * Rutas del `NavHost`. La clave es el destino, no la pantalla: al agregar una pantalla de
 * una feature se registra acá y se enlaza su `composable` en [AppNavHost].
 *
 * Pendiente cuando lleguen HU-01 y HU-02: destino de ingreso y guarda por sesión.
 */
object AppRoutes {
    const val Start = "start"
    const val Home = "home"
    const val PickupLocation = "profile/pickup-location"
    const val Register = "auth/register"
    const val CompleteProfile = "profile/complete"
    const val Profile = "profile"
    const val CreateOrder = "orders/create"
    const val OrderPublishedResult = "order_published"
    const val ExpiredArg = "expired"
    const val Login = "auth/login?$ExpiredArg={$ExpiredArg}"

    fun login(expired: Boolean = false): String = "auth/login?$ExpiredArg=$expired"
}
