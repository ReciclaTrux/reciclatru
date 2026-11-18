# view/navigation

Rutas y `NavHost` de la app. Define **dónde** se puede ir y qué se muestra al iniciar.

| Archivo | Qué hace |
| --- | --- |
| `AppRoutes.kt` | Constantes de ruta. |
| `AppNavHost.kt` | Registra cada pantalla y su `composable`; reacciona al cierre de sesión. |

- Las pantallas no viven acá: cada una está en `view/<dominio>/`.
- El estado de sesión llega desde `viewmodel/session/AppSessionViewModel`.
- Al agregar una pantalla, se agrega su ruta y se registra en `AppNavHost`.
- Las guardas solo redirigen por sesión o rol para mejorar la UX; el backend sigue
  autorizando.
