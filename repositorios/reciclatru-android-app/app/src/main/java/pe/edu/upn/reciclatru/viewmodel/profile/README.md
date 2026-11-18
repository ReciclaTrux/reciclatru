# viewmodel/profile

| Archivo | Qué hace |
| --- | --- |
| `LocationViewModel.kt` | Eventos, casos de uso, errores, debounce de búsqueda y transición entre pasos de HU-04. |
| `LocationUiState.kt` | Estado inmutable con `LocationStep`, sugerencias, indicadores y errores. |
| `ProfileViewModel.kt` | Carga y guarda el perfil; valida nombre y teléfono. |
| `ProfileUiState.kt` | Estado del formulario de perfil. |

El autocomplete tiene un debounce de 300 ms en `LocationViewModel` y pasa hasta cuatro
sugerencias a `RtMapPicker`. Las pantallas están en `view/profile/`.
