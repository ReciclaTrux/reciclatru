package pe.edu.upn.reciclatru.core.navigation

/**
 * Rutas del `NavHost`. La clave es el destino, no la pantalla: al agregar una pantalla de
 * una feature se registra acá y se enlaza su `composable` en [AppNavHost].
 *
 * Pendiente cuando lleguen HU-01 y HU-02: destino de ingreso y guarda por sesión.
 */
object AppRoutes {
    const val Home = "home"
    const val PickupLocation = "profile/pickup-location"
}
