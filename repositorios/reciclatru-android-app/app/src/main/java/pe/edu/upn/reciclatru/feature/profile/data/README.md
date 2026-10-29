# profile / data

| Archivo | Qué hace |
| --- | --- |
| `ProfileApi.kt` | `PUT /location-data-management/{id}/pickup-point/update` (provisional hasta el OpenAPI). |
| `PickupPointDtos.kt` | DTOs con `@SerializedName` en todos los campos. |
| `PickupLocationMapper.kt` | DTO ↔ dominio, conservando lo enviado si la API omite campos. |
| `AndroidGeocoderDataSource.kt` | `android.location.Geocoder` en el dispatcher de IO: búsqueda, sugerencias y lectura del pin. |
| `GeocodingRepositoryImpl.kt` | Implementa `GeocodingRepository`. |
| `ProfileRepositoryImpl.kt` | Sesión + `apiCall` + mapper. |
| `ProfileModule.kt` | `@Binds`, `ProfileApi` y `Geocoder` para Hilt. |

Reglas:

- Todo DTO lleva `@SerializedName` (R8 no rompe el JSON en release).
- El error de la API se traduce con `apiCall`; el repositorio no muestra mensajes en UI.
- No se inventan operaciones: si el contrato no existe, se documenta la operación
  provisional en `ProfileApi`.
- La búsqueda sesga la consulta a Trujillo cuando el texto no menciona la ciudad, para no
  devolver calles con el mismo nombre en otros países.
