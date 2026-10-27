# core/navigation

Rutas tipadas, `NavHost` y guardas de navegación de la app. Define **dónde** se puede ir
(login, pedidos, explorar, seguimiento) y qué se muestra al iniciar según haya sesión o no.

- Las pantallas no viven acá: cada una está en `feature/<dominio>/ui/`.
- Las guardas solo redirigen por sesión o rol para mejorar la UX; el backend sigue autorizando.
- Al agregar una pantalla, se registra su ruta y se enlaza el `composable` correspondiente.
