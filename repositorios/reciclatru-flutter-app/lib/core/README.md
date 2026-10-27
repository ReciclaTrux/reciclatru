# core (Flutter)

Código transversal a todas las features: red, sesión, navegación y sistema de diseño. No
contiene lógica de negocio, pantallas ni entidades de un dominio.

- `network/` — cliente HTTP (Dio) e interceptores.
- `session/` — token JWT, sesión y rol activo.
- `router/` — go_router, rutas y guardas de navegación.
- `designsystem/` — Atomic Design ([README](designsystem/README.md)).

Si algo solo lo usa una feature, va en `features/<dominio>/`, no acá.
