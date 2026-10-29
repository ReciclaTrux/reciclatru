# profile / ui

| Archivo | Qué hace |
| --- | --- |
| `LocationScreen.kt` | HU-04 en dos pasos (mapa y dirección) + paso guardado; solo dibuja estado. |
| `LocationViewModel.kt` | Eventos, casos de uso, errores, debounce de búsqueda y transición entre pasos. |
| `LocationUiState.kt` | Estado inmutable con `LocationStep`, sugerencias, indicadores y errores. |

La pantalla usa `hiltViewModel()` y `collectAsStateWithLifecycle()`. El autocomplete tiene un
debounce de 300 ms en el ViewModel y pasa hasta cuatro sugerencias a `RtMapPicker`. Los
`testTag` están en `map_punto_recojo`, `input_direccion`, `input_distrito`,
`btn_guardar_ubicacion`, `btn_listo_ubicacion`, `alert_ubicacion_info` y
`alert_error_ubicacion`.

La próxima HU (HU-03) agrega `ProfileScreen`, `EditProfileScreen` y `ProfileViewModel` en
esta misma carpeta.
