# di

Módulos Hilt y calificadores. Es el único lugar donde se enlazan interfaces con
implementaciones.

| Archivo | Qué provee |
| --- | --- |
| `Qualifiers.kt` | `@IoDispatcher` y `@ApplicationScope`. |
| `DispatcherModule.kt` | `Dispatchers.IO` y un `CoroutineScope` que vive lo mismo que la app. |
| `SessionModule.kt` | El `DataStore<Preferences>` privado de la sesión. |
| `NetworkModule.kt` | OkHttp y Retrofit con `BuildConfig.API_BASE_URL`. |
| `AuthModule.kt`, `ProfileModule.kt`, `OrdersModule.kt`, `ExploreModule.kt`, `HealthModule.kt` | `@Binds` repositorio ↔ implementación y las APIs Retrofit de cada dominio. |

- No crear instancias manuales de dependencias dentro de Compose ni de los ViewModels.
- Los tests de UI sustituyen módulos con `@TestInstallIn` en lugar de tocar estos módulos.
