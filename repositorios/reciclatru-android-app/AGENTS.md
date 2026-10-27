# AGENTS.md — Android (ReciclaTru)

Cliente principal del proyecto. Kotlin + Jetpack Compose + Material 3, arquitectura MVVM
por feature e inyección de dependencias con Hilt. Paquete base:
`pe.edu.upn.reciclatru`.

Lee primero `../../AGENTS.md` y la constitución en `../../.specify/memory/constitution.md`.
Antes de empezar una HU, sigue `GUIA-HU.md`: capas, Hilt, uso obligatorio del sistema de
diseño, tests y checklist del PR.

## Arquitectura

Una **feature = un dominio de negocio**, con el mismo nombre que en Flutter: `auth`, `profile`, `orders`, `explore`, `tracking`. Cada feature se divide en
tres capas:

```text
app/src/main/java/pe/edu/upn/reciclatru/
├── core/                      # transversal, sin lógica de un dominio
│   ├── network/               # cliente HTTP, interceptores, errores de red
│   ├── session/               # token JWT, sesión y rol activo
│   ├── navigation/            # rutas, NavHost y guardas de navegación
│   └── di/                    # módulos Hilt globales
├── ui/
│   └── designsystem/          # Atomic Design: theme/ atoms/ molecules/ organisms/
├── feature/
│   ├── auth/                  # ui/ domain/ data/
│   ├── profile/
│   ├── orders/
│   ├── explore/
│   └── tracking/
└── MainActivity.kt
```

### Qué va en cada capa

- **`ui/`**: pantallas Compose (`*Screen.kt`), ViewModels (`*ViewModel.kt`), estados de
  UI y navegación de la feature. Es la **única** capa que conoce el ViewModel.
  Los componentes visuales reutilizables **no** van acá: van a `ui/designsystem`.
- **`domain/`**: modelos de dominio, casos de uso y contratos de repositorio
  (`interface`). Kotlin puro, sin Android, sin Retrofit y sin anotaciones de Hilt.
- **`data/`**: implementación de los repositorios, DTOs de la API, mappers DTO ↔ dominio,
  fuentes de datos (Retrofit/DataStore) y el módulo Hilt que une las interfaces de
  `domain` con sus implementaciones.

Flujo de dependencias: `ui → domain ← data`. La capa `ui` nunca importa DTOs ni clases de
`data`; `domain` no depende de Android.

## Reglas

1. **MVVM**: cada pantalla tiene un `ViewModel` que expone un `StateFlow<UiState>`
   inmutable, recibe eventos y llama casos de uso. La pantalla solo dibuja estado y envía
   eventos; no hace llamadas de red ni cálculos de negocio.
2. **Hilt**: `@HiltAndroidApp` en la clase `Application`, `@AndroidEntryPoint` en
   actividades/fragmentos y `@HiltViewModel` en los ViewModels. Módulos globales en
   `core/di`; el binding repositorio ↔ implementación de cada feature en su `data/`.
   No crear instancias manuales de repositorios ni de Retrofit dentro de Compose.
3. **Nunca validar una regla de negocio solo en la app.** El backend es la fuente de
   verdad: permisos por rol, estados del pedido, privacidad de teléfono/dirección y
   cualquier restricción se validan en la API. La app puede deshabilitar botones o mostrar
   mensajes para mejorar la UX, pero siempre debe manejar el error del servidor.
4. **Atomic Design**: todo lo visual reutilizable vive en `ui/designsystem` y sigue el
   sistema de diseño (ver la sección *Sistema de diseño*).
   - Componentes stateless con prefijo `Rt`: reciben datos y callbacks; nunca ViewModels,
     repositorios ni modelos de dominio.
   - Los átomos envuelven Material 3, no lo reemplazan.
   - Todo componente lleva `@Preview` con los mismos estados que su vista previa en el
     sistema.
   - Las pantallas viven en `feature/*/ui`, no en el sistema de diseño.
5. **Privacidad**: no persistir la ubicación del reciclador; no mostrar teléfono ni
   dirección si el DTO del backend no los incluye para el rol/estado actual.
6. **Componentes clave con `testTag`** (p. ej. `RtButton` recibe `modifier` y la pantalla
   aplica `Modifier.testTag("btn_publicar_pedido")`) para tests de UI estables.
7. **Sin dependencias nuevas** sin justificación en el plan de la HU; prohibido Firebase,
   Google Maps API y servicios de pago o con API key.
8. **Español** en nombres de pantallas visibles, mensajes y documentación. El código
   (clases, funciones, columnas) en inglés, salvo los nombres de dominio ya acordados.

## Sistema de diseño (Atomic Design)

La única fuente de estilos y componentes es el sistema de diseño de ReciclaTru:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2
(leer `project/README.md`, `project/tokens.json`, `project/implementacion.md` y la guía
`project/components/<Componente>/README.md` antes de crear o usar un componente).
Ninguna pantalla define colores, tamaños ni textos de estado por su cuenta.

- **Niveles**: `theme` (tokens → Material 3 + extensión propia), `atoms`, `molecules`,
  `organisms`. No hay nivel de plantillas: las pantallas viven en la feature.
- **Carpetas**: `ui/designsystem/{theme,atoms,molecules,organisms}`.
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
- **Íconos**: Material Symbols Rounded (`Icons.Rounded.*`), con el mismo nombre en ambas apps.
- **Textos**: los textos exactos de diálogos, vacíos y errores salen de las guías de
  `RtConfirmDialog`, `RtEmptyState`, `RtInlineAlert` y `RtErrorState`; se usan tal cual.
  Se tutea, sin emojis ni "exitosamente".
- **Estados de pantalla**: toda lista o detalle tiene carga (`RtSpinner`), vacío
  (`RtEmptyState`) y error con Reintentar (`RtErrorState`; con datos previos,
  `RtSnackbar`).
- **Catálogo cerrado**: el sistema ya está definido (tokens y 33 componentes `Rt*`: 9
  átomos, 13 moléculas, 11 organismos; la lista está en el README de cada nivel dentro de
  `ui/designsystem`). **No se crean componentes nuevos, variantes locales ni equivalentes**, ni
  dentro de una feature. Si el sistema tiene el componente, se usa; si falta algo para una
  HU, se propone primero en el sistema y, aprobado, se implementa en las dos apps con el
  mismo nombre. Nunca se resuelve con un widget suelto en la pantalla.
- **Construcción del sistema por sprint**: antes de cada sprint se implementan, en una rama
  aparte, los componentes que necesitan las HU de ese sprint (según el inventario por HU del
  sistema) y sus dependencias internas. Orden: tema → átomos → moléculas → organismos. Las HU
  solo los usan; si falta algo, se propone primero en el sistema. Por cada componente: leer su
  guía, respetar el nombre `Rt…` y los parámetros de "Qué recibe", y crear `@Preview` con los
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

Desde `repositorios/reciclatru-android-app/` (en Windows usar `.\gradlew.bat`):

```bash
./gradlew assembleDebug        # compilar APK de debug
./gradlew test                 # tests unitarios JVM (ViewModels, mappers)
./gradlew connectedAndroidTest # tests instrumentados de UI (requiere emulador/dispositivo)
./gradlew lint                 # Android Lint
./gradlew :app:compileDebugKotlin  # verificación rápida de compilación Kotlin
```

Antes de un PR: `test`, `lint` y `assembleDebug` en verde. Los tests de UI usan
repositorios falsos inyectados con Hilt (`@TestInstallIn` / `HiltTestApplication`), nunca
la API real.

## Convenciones de código

- Compose con Material 3; colores, tipografía, espaciado, radios y tamaños **solo** desde
  `ui/designsystem/theme`, nunca valores sueltos (`Color(0x…)`, `16.dp`).
- Un archivo por pantalla, ViewModel y caso de uso relevante; nombres en PascalCase.
- `modifier: Modifier = Modifier` como parámetro de todo composable público.
- Estados de UI explícitos (`Loading`, `Success`, `Empty`, `Error`) y textos de error
  entendibles para el vecino/reciclador.
- No dejar código muerto ni TODOs sin dueño ni clave de Jira.
