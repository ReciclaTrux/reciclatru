# ReciclaTru — Flutter

Cliente iOS/multiplataforma de **ReciclaTru**, que conecta a vecinos de Trujillo con
recicladores (ODS 12). Flutter + Material 3, **Riverpod** para estado e inyección y
**go_router** para navegación. Comparte con la app Android los mismos dominios y los mismos
componentes `Rt*`.

## Estructura

```text
lib/
├── main.dart
├── core/
│   ├── design_system/   # sistema de diseño: theme/ atoms/ molecules/ organisms/
│   ├── network/  router/  session/
└── features/            # auth, profile, orders, explore, tracking (presentation/ domain/ data/)
```

## Sistema de diseño

La interfaz usa **solo** el sistema de diseño de ReciclaTru (Atomic Design, componentes
`Rt*` compartidos con la app Android):
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

No se crean componentes fuera de ese catálogo. Detalle y estado por HU en
[`lib/core/design_system/README.md`](lib/core/design_system/README.md#componentes-por-hu).

## Comandos

```bash
flutter pub get
flutter run
flutter analyze
flutter test
```

## Más información

Reglas del proyecto y de arquitectura: [`AGENTS.md`](AGENTS.md).
