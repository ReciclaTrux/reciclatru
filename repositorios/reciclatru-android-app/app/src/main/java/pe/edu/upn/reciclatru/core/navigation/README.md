# core/navigation

Rutas y `NavHost` de la app. Define **dónde** se puede ir y qué se muestra al iniciar.

| Archivo | Qué hace |
| --- | --- |
| `AppRoutes.kt` | Constantes de ruta (`Home`, `PickupLocation`). |
| `AppNavHost.kt` | Registra cada pantalla de feature y su `composable`. |

- Las pantallas no viven acá: cada una está en `feature/<dominio>/ui/`.
- Al agregar una pantalla, se agrega su ruta y se registra en `AppNavHost`.
- El destino inicial es un marcador temporal (`Home`) hasta que exista el ingreso de
  HU-01/HU-02.
- Las guardas solo redirigen por sesión o rol para mejorar la UX; el backend sigue
  autorizando.
