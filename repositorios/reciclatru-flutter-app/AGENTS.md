# AGENTS.md — Flutter (ReciclaTru)

Cliente iOS/multiplataforma. Flutter + Material 3, **Riverpod** para estado e inyección y
**go_router** para navegación. Se trabaja después de Android, pero comparte la misma API
y los mismos nombres de dominio. Lee primero `../../AGENTS.md` y la constitución en
`../../.specify/memory/constitution.md`.

## Arquitectura

Una **feature = un dominio de negocio**, con el mismo nombre que en Android: `auth`, `profile`, `orders`, `explore`, `tracking`. Capas:

```text
lib/
├── main.dart
├── core/                      # transversal, sin lógica de un dominio
│   ├── network/               # cliente HTTP, interceptores, errores de red
│   ├── session/               # token JWT, sesión y rol activo
│   ├── router/                # go_router, rutas y guardas de sesión/rol
│   └── design_system/         # Atomic Design: theme/ atoms/ molecules/ organisms/
└── features/
    ├── auth/                  # presentation/ domain/ data/
    ├── profile/
    ├── orders/
    ├── explore/
    └── tracking/
```

### Qué va en cada capa

- **`presentation/`**: pantallas/widgets propios de la feature, `ConsumerWidget` /
  `ConsumerStatefulWidget`, providers de presentación (`Notifier`, `AsyncNotifier`) y
  navegación de la feature. Es la **única** capa que conoce providers de estado y
  `go_router`. Los componentes visuales reutilizables **no** van acá: van a
  `core/design_system`.
- **`domain/`**: entidades de dominio, casos de uso y contratos de repositorio
  (`abstract class`). Dart puro, sin Flutter, sin Dio y sin Riverpod.
- **`data/`**: implementación de repositorios, DTOs (`fromJson`/`toJson`), mappers
  DTO ↔ dominio, fuentes de datos (Dio/almacenamiento seguro) y los providers que
  inyectan las implementaciones.

Flujo de dependencias: `presentation → domain ← data`. `presentation` nunca importa DTOs
ni clases de `data`; `domain` no importa Flutter.

## Reglas

1. **Riverpod**:
   - Providers globales (cliente HTTP, sesión) en `core/`, con `Provider`/`Notifier`.
   - Cada feature expone sus providers en `data/` (repositorio) y `presentation/`
     (estado de pantalla), con `AsyncNotifier` para carga asíncrona.
   - En tests, sustituir dependencias con `ProviderScope(overrides: [...])`; nunca llamar
     a la API real.
   - No usar `setState` para estado de negocio ni `Provider.of` con `listen: false`
     esparcido por el árbol: el estado vive en el provider.
2. **go_router**: las rutas viven en `core/router`; las guardas de sesión/rol redirigen,
   pero **el backend sigue siendo quien autoriza**. La pantalla no decide permisos.
3. **Nunca validar una regla de negocio solo en la app.** Permisos por rol, estados del
   pedido, privacidad de teléfono/dirección y restricciones se validan en la API. La app
   puede deshabilitar acciones por UX, pero siempre maneja el error del servidor.
4. **Atomic Design**: todo lo visual reutilizable vive en `lib/core/design_system` y
   sigue el sistema de diseño (ver la sección *Sistema de diseño*).
   - Widgets stateless con prefijo `Rt`: reciben datos y callbacks; nunca providers,
     repositorios ni modelos de dominio.
   - Los átomos envuelven Material 3, no lo reemplazan.
   - Todo componente tiene un ejemplo (Widgetbook) con los mismos estados que su vista
     previa en el sistema.
   - Las pantallas viven en `features/*/presentation`, no en el sistema de diseño.
5. **Privacidad**: no persistir la ubicación del reciclador; no mostrar teléfono ni
   dirección si el DTO del backend no los incluye para el rol/estado actual.
6. **Componentes clave con `Key`** (p. ej. `const Key('btn_publicar_pedido')`) para tests
   de widget estables.
7. **Sin dependencias nuevas** sin justificación en el plan de la HU; prohibido Firebase,
   Google Maps API y servicios de pago o con API key.
8. **Español** en textos visibles, mensajes y documentación; código en inglés salvo los
   nombres de dominio acordados.

## Sistema de diseño (Atomic Design)

La única fuente de estilos y componentes es el sistema de diseño de ReciclaTru:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2
(leer `project/README.md`, `project/tokens.json`, `project/implementacion.md` y la guía
`project/components/<Componente>/README.md` antes de crear o usar un componente).
Ninguna pantalla define colores, tamaños ni textos de estado por su cuenta.

- **Niveles**: `theme` (tokens → Material 3 + extensión propia), `atoms`, `molecules`,
  `organisms`. No hay nivel de plantillas: las pantallas viven en la feature.
- **Carpetas**: `lib/core/design_system/{theme,atoms,molecules,organisms}`.
- **Nombres**: prefijo `Rt` y el mismo nombre y parámetros en Android y Flutter
  (`RtButton`, `RtTextField`, `RtOrderCard`, `RtStatusChip`…). Si el sistema ya define el
  componente, se implementa con su nombre y los parámetros de "Qué recibe" de su guía.
- **Composición**: un átomo solo usa tokens; una molécula usa átomos; un organismo usa
  moléculas y átomos. Las pantallas solo usan componentes de este sistema.
- **Tokens**: `tokens.json` es la fuente. Los colores se mapean a Material 3 como indica
  `implementacion.md`; lo que Material no tiene (`ods-12`, `status-*`, `warning-*`,
  `info-*`, `disabled-*`, tamaños) va en una extensión propia. Si un valor cambia en el
  sistema, se actualiza en las dos apps en el mismo PR.
- **Reglas de uso**: texto de contenido mínimo 16 (`body`); todo lo tocable mínimo 48
  (`touch-target`); una sola acción primaria por pantalla; el color nunca va solo (los
  estados llevan ícono y texto); `outline-strong` para bordes de controles; el rojo
  (`error`) solo para errores y acciones destructivas.
- **Íconos**: Material Symbols Rounded (`Icons.*_rounded`), con el mismo nombre en ambas apps.
- **Textos**: los textos exactos de diálogos, vacíos y errores salen de las guías de
  `RtConfirmDialog`, `RtEmptyState`, `RtInlineAlert` y `RtErrorState`; se usan tal cual.
  Se tutea, sin emojis ni "exitosamente".
- **Estados de pantalla**: toda lista o detalle tiene carga (`RtSpinner`), vacío
  (`RtEmptyState`) y error con Reintentar (`RtErrorState`; con datos previos,
  `RtSnackbar`).
- **Catálogo cerrado**: el sistema ya está definido (tokens y 33 componentes `Rt*`: 9
  átomos, 13 moléculas, 11 organismos; la lista está en el README de cada nivel dentro de
  `lib/core/design_system`). **No se crean componentes nuevos, variantes locales ni equivalentes**, ni
  dentro de una feature. Si el sistema tiene el componente, se usa; si falta algo para una
  HU, se propone primero en el sistema y, aprobado, se implementa en las dos apps con el
  mismo nombre. Nunca se resuelve con un widget suelto en la pantalla.
- **Construcción del sistema por sprint**: antes de cada sprint se implementan, en una rama
  aparte, los componentes que necesitan las HU de ese sprint (según el inventario por HU del
  sistema) y sus dependencias internas. Orden: tema → átomos → moléculas → organismos. Las HU
  solo los usan; si falta algo, se propone primero en el sistema. Por cada componente: leer su
  guía, respetar el nombre `Rt…` y los parámetros de "Qué recibe", y crear un ejemplo en Widgetbook con los
  mismos estados que su vista previa.
- **Sprint 1 (HU-01 a HU-04), 17 componentes**: átomos `RtButton`, `RtTextField`, `RtSpinner`,
  `RtIconButton`, `RtAvatar`, `RtBadge`; moléculas `RtRoleSelector`, `RtInlineAlert`,
  `RtSearchField`, `RtContactRow`, `RtConfirmDialog`, `RtSnackbar`, `RtErrorState`;
  organismos `RtTopAppBar`, `RtBottomNav`, `RtMapPicker`, `RtActionBar`.
- **Referencia de pantallas**: el prototipo https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh muestra cada pantalla armada con
  estos componentes (registro, login, perfil, mapa, etc.). Cada pantalla de una HU se
  construye igual que su tablero; no se rediseña.
- **Qué usa una pantalla**: si el catálogo tiene una molécula u organismo para eso, se usa;
  no se rearma con átomos (p. ej. una tarjeta de pedido es `RtOrderCard`, no chips y textos
  sueltos). Los átomos van directos solo cuando ningún componente mayor cubre ese caso
  (p. ej. `RtTextField` y `RtButton` en registro e ingreso).

## Compilar y probar

Desde `repositorios/reciclatru-flutter-app/`:

```bash
flutter pub get       # instalar dependencias (solo si ya están declaradas)
flutter run           # correr en emulador/dispositivo
flutter analyze       # análisis estático (debe salir sin issues)
flutter test          # tests unitarios y de widget
flutter test --coverage
flutter build ios     # build iOS (requiere macOS/Xcode)
flutter build apk     # build Android de verificación
```

Cuando esté configurado Widgetbook, el catálogo se levanta con
`flutter run -t widgetbook/main.dart` (o el entrypoint acordado en el repo).

Antes de un PR: `flutter analyze`, `flutter test` y build del target correspondiente en
verde. Los tests usan repositorios falsos vía overrides de Riverpod.

## Convenciones de código

- Material 3; colores, tipografía, espaciado, radios y tamaños **solo** desde
  `core/design_system/theme`, nunca valores sueltos (`Color(0x…)`, `16.0`).
- Widgets con `const` cuando sea posible; nombres en PascalCase y archivos en
  `snake_case.dart`.
- Estados asíncronos con `AsyncValue` (`loading`, `data`, `error`) y un estado vacío
  explícito cuando la lista no tiene elementos.
- No dejar código muerto ni TODOs sin dueño ni clave de Jira.
