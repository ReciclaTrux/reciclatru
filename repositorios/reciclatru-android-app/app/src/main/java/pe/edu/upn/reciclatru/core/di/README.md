# core/di

Módulos Hilt globales: cliente HTTP, almacenamiento seguro del token, dispatchers y
utilidades compartidas.

- El binding repositorio ↔ implementación de cada feature va en `feature/<dominio>/data/`,
  no acá.
- No crear instancias manuales de dependencias dentro de Compose ni de los ViewModels.
- Los tests de UI sustituyen módulos con `@TestInstallIn` en lugar de tocar estos módulos.
