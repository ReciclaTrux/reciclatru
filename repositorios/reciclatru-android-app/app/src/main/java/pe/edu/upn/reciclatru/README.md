# pe.edu.upn.reciclatru

Mapa de paquetes de la app. Arquitectura **MVVM** organizada por tipo de clase y, dentro de
cada paquete, por dominio de negocio (`auth`, `profile`, `orders`, `explore`).

```text
reciclatru/
├── model/          # modelos de dominio (Kotlin puro)
├── view/           # pantallas Compose (*Screen.kt), formatters y navegación
├── viewmodel/      # ViewModels y estados de UI (*UiState.kt)
├── domain/
│   ├── usecase/    # casos de uso
│   └── repository/ # contratos (interface) de repositorios y fuentes
├── data/
│   ├── remote/     # cliente HTTP, errores, y api/ dto/ mapper/ por dominio
│   ├── local/      # sesión (DataStore), preferencias, geocoder, fotos
│   └── repository/ # implementaciones de los repositorios
├── di/             # módulos Hilt y calificadores
└── ui/designsystem/ # sistema de diseño: theme/ atoms/ molecules/ organisms/
```

Flujo de dependencias: `view → viewmodel → domain ← data`.

- `view` conoce a `viewmodel`; `viewmodel` nunca conoce a `view` ni a Compose.
- `viewmodel` solo llama casos de uso (`domain/usecase`); no importa DTOs ni clases de `data`.
- `domain` y `model` no dependen de Android ni de Retrofit.
- `data` implementa los contratos de `domain/repository`.

El sistema de diseño vive en `ui/designsystem/` ([README](ui/designsystem/README.md)).
El dominio `tracking` (HU-13 a HU-16) aún no existe: cuando se implemente, tendrá su
carpeta en cada capa, igual que los demás dominios.
