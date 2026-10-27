# core (Android)

Código transversal a todas las features: red, sesión, navegación, inyección de dependencias y
sistema de diseño. No contiene lógica de negocio, pantallas ni modelos de un dominio.

- `network/` — cliente HTTP e interceptores.
- `session/` — token JWT, sesión y rol activo.
- `navigation/` — rutas, `NavHost` y guardas de navegación.
- `di/` — módulos Hilt globales.
- `designsystem/` — Atomic Design ([README](designsystem/README.md)).

Si algo solo lo usa una feature, va en `feature/<dominio>/`, no acá.
