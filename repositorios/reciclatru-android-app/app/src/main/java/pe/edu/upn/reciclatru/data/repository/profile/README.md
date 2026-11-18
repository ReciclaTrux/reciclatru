# data/repository/profile

| Archivo | Qué hace |
| --- | --- |
| `ProfileRepositoryImpl.kt` | Punto de recojo: sesión + `apiCall` + mapper. |
| `UserProfileRepositoryImpl.kt` | Perfil del usuario. |
| `GeocodingRepositoryImpl.kt` | Implementa `GeocodingRepository` con `AndroidGeocoderDataSource` (`data/local/profile/`). |

Otros archivos de este dominio:

- `data/remote/api/profile/ProfileApi.kt`: `PUT /location-data-management/{id}/pickup-point/update`
  (provisional hasta el OpenAPI).
- `data/remote/dto/profile/`: DTOs con `@SerializedName` en todos los campos.
- `data/remote/mapper/profile/PickupLocationMapper.kt`: DTO ↔ dominio, conservando lo enviado
  si la API omite campos.
- `di/ProfileModule.kt`: `@Binds`, `ProfileApi` y `Geocoder` para Hilt.

Reglas:

- El error de la API se traduce con `apiCall`; el repositorio no muestra mensajes en UI.
- No se inventan operaciones: si el contrato no existe, se documenta la operación
  provisional en `ProfileApi`.
- La búsqueda sesga la consulta a Trujillo cuando el texto no menciona la ciudad, para no
  devolver calles con el mismo nombre en otros países.
