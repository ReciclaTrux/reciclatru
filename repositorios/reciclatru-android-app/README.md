# ReciclaTru — Android

Cliente principal de **ReciclaTru**, que conecta a vecinos de Trujillo con recicladores
(ODS 12). Kotlin + Jetpack Compose + Material 3, arquitectura MVVM por feature e
inyección de dependencias con Hilt. Paquete base: `pe.edu.upn.reciclatru`.

## Estructura

```text
app/src/main/java/pe/edu/upn/reciclatru/
├── core/                  # network, session, navigation, di
├── ui/designsystem/       # sistema de diseño: theme/ atoms/ molecules/ organisms/
├── feature/               # auth, profile, orders, explore, tracking (ui/ domain/ data/)
└── MainActivity.kt
```

## Sistema de diseño

La interfaz usa **solo** el sistema de diseño de ReciclaTru (Atomic Design, componentes
`Rt*` compartidos con la app Flutter):
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

No se crean componentes fuera de ese catálogo. Detalle, estado de cada componente y la
sección **Componentes por HU** en
`app/src/main/java/pe/edu/upn/reciclatru/ui/designsystem/README.md#componentes-por-hu`.

## Compilar y probar

En Windows usar `.\gradlew.bat`:

```bash
./gradlew assembleDebug   # APK de debug
./gradlew test            # tests unitarios
./gradlew lint            # Android Lint
```

## Más información

Reglas del proyecto y de arquitectura: [`AGENTS.md`](AGENTS.md).
