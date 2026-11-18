# data/remote

Cliente HTTP compartido y todo lo que rodea una llamada. Los endpoints, DTOs y mappers de
cada dominio viven en `api/<dominio>/`, `dto/<dominio>/` y `mapper/<dominio>/`.

| Archivo | Qué hace |
| --- | --- |
| `AuthInterceptor.kt` | Agrega `Bearer <token>` cuando `SessionStore` tiene sesión. |
| `NetworkException.kt` | `NetworkErrorKind`, `NetworkException`, `apiCall { }` y `toUserMessage()`. |
| `ApiUrls.kt` | Construcción de URLs de la API. |

OkHttp y Retrofit se proveen en `di/NetworkModule.kt`.

Reglas:

- No decide permisos: solo transporta la petición y devuelve éxito, error de red o error
  de la API con un mensaje en español.
- Toda llamada de un repositorio pasa por `apiCall`; así el ViewModel recibe un error que
  puede mostrar sin conocer Retrofit.
- Todo DTO lleva `@SerializedName` (R8 no rompe el JSON en release).
