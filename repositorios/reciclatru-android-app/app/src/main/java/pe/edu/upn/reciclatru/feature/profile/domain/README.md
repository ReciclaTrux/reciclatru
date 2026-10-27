# profile / domain

| Archivo | Qué define |
| --- | --- |
| `GeoPoint.kt` | Punto con validación de rango y `TrujilloCenter` como punto de partida. |
| `PickupLocation.kt` | `PickupLocation` y `PickupAddress` (dirección y distrito). |
| `GeocodingRepository.kt` | Contrato de búsqueda, sugerencias y lectura de direcciones + `AddressSuggestion` y `GeocodingException`. |
| `ProfileRepository.kt` | Contrato para guardar el punto de recojo. |
| `SearchAddressUseCase.kt` | Texto → punto. |
| `SuggestAddressesUseCase.kt` | Texto → hasta cuatro sugerencias con su punto. |
| `DescribePointUseCase.kt` | Punto → calle y distrito. |
| `SavePickupLocationUseCase.kt` | Valida lo mínimo y guarda en el repositorio. |

Kotlin puro: sin Android, sin Retrofit y sin anotaciones de Hilt. La próxima HU de perfil
(HU-03) agrega acá `Profile`, `ContactInfo` y sus casos de uso.
