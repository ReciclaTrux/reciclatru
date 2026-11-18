# view/profile

**Dominio:** perfil del usuario — datos propios, de contacto y punto de recojo.

| HU | Estado |
| --- | --- |
| HU-03 (completar perfil) | pantallas `ProfileScreen` y `CompleteProfileScreen` |
| HU-04 (punto de recojo) | `LocationScreen`, implementada como ejemplo de corte vertical |

HU-04 usa `RtMapPicker`, `RtSearchField`, `RtTextField`, `RtInlineAlert` y `RtActionBar`.
El mapa es MapLibre con el estilo `positron` de OpenFreeMap y el geocoder es el de Android
(con autocomplete y debounce de 300 ms); la ubicación se guarda en la API y requiere sesión.

| Archivo | Qué hace |
| --- | --- |
| `LocationScreen.kt` | HU-04 en dos pasos (mapa y dirección) + paso guardado; solo dibuja estado. |
| `ProfileScreen.kt`, `CompleteProfileScreen.kt` | Ver y completar los datos del perfil. |
| `ProfileFields.kt` | Campos reutilizados por las pantallas de perfil. |

Los `testTag` de HU-04 están en `map_punto_recojo`, `input_direccion`, `input_distrito`,
`btn_guardar_ubicacion`, `btn_listo_ubicacion`, `alert_ubicacion_info` y
`alert_error_ubicacion`. Los ViewModels están en `viewmodel/profile/`.
