# domain/usecase/profile

| Archivo | Qué define |
| --- | --- |
| `SearchAddressUseCase.kt` | Texto → punto. |
| `SuggestAddressesUseCase.kt` | Texto → hasta cuatro sugerencias con su punto. |
| `DescribePointUseCase.kt` | Punto → calle y distrito. |
| `SavePickupLocationUseCase.kt` | Valida lo mínimo y guarda en el repositorio. |
| `GetProfileUseCase.kt`, `UpdateProfileUseCase.kt` | Leer y actualizar el perfil. |

Contratos en `domain/repository/profile/` (`GeocodingRepository`, `ProfileRepository`,
`UserProfileRepository`) y modelos en `model/profile/` (`GeoPoint` con `TrujilloCenter` como
punto de partida, `PickupLocation`, `UserProfile`). Sin Android ni Retrofit; los casos de uso
solo llevan `@Inject`.
