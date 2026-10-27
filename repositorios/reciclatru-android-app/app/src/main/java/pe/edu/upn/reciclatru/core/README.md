# core (Android)

Código transversal a todas las features. No contiene lógica de negocio, pantallas ni
modelos de un dominio.

```text
core/
├── network/     # OkHttp, Retrofit, interceptor y errores
├── session/     # Session, UserRole y SessionStore
├── navigation/  # AppRoutes y AppNavHost
└── di/          # módulos Hilt globales y calificadores
```

El sistema de diseño no está acá: vive en `ui/designsystem/`
([README](../ui/designsystem/README.md)).

Si algo solo lo usa una feature, va en `feature/<dominio>/`, no acá.
