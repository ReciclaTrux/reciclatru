# core/network

Cliente HTTP compartido y todo lo que rodea una llamada.

| Archivo | Qué hace |
| --- | --- |
| `NetworkModule.kt` | OkHttp (timeouts, logging solo en debug, `Authorization` tachado) y Retrofit con `BuildConfig.API_BASE_URL`. |
| `AuthInterceptor.kt` | Agrega `Bearer <token>` cuando `SessionStore` tiene sesión. |
| `NetworkException.kt` | `NetworkErrorKind`, `NetworkException`, `apiCall { }` y `toUserMessage()`. |

Reglas:

- No define DTOs ni repositorios de un dominio: eso vive en `feature/<dominio>/data/`.
- No decide permisos: solo transporta la petición y devuelve éxito, error de red o error
  de la API con un mensaje en español.
- Toda llamada de un repositorio pasa por `apiCall`; así el ViewModel recibe un error que
  puede mostrar sin conocer Retrofit.
