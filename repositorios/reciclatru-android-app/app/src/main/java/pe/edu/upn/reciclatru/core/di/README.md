# core/di

Módulos Hilt globales y calificadores.

| Archivo | Qué provee |
| --- | --- |
| `Qualifiers.kt` | `@IoDispatcher` y `@ApplicationScope`. |
| `DispatcherModule.kt` | `Dispatchers.IO` y un `CoroutineScope` que vive lo mismo que la app. |
| `SessionModule.kt` | El `DataStore<Preferences>` privado de la sesión. |

- El binding repositorio ↔ implementación de cada feature va en `feature/<dominio>/data/`,
  no acá.
- No crear instancias manuales de dependencias dentro de Compose ni de los ViewModels.
- Los tests de UI sustituyen módulos con `@TestInstallIn` en lugar de tocar estos módulos.
