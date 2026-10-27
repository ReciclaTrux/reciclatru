# Guía para desarrollar una HU (Android)

Esta guía es para cualquier persona o agente que vaya a implementar una historia de
usuario en la app Android. Sigue el orden y no inventes piezas: la arquitectura, el
sistema de diseño y los nombres ya están decididos.

Antes de empezar, lee también:

1. [`AGENTS.md`](AGENTS.md) — reglas del cliente Android.
2. `../../AGENTS.md` — flujo de ramas, commits y PR del monorepo.
3. Tu HU en Jira y su tablero del prototipo:
   https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh

## 1. Qué se entrega en una HU

Una HU es un **corte vertical**: pantalla + consumo real de la API + tests, demostrable de
punta a punta. No se entrega "solo la UI" ni "solo el endpoint".

- Rama: `feature/SCRUM-XX-descripcion-corta`.
- Un PR por HU, con `SCRUM-XX` en el título.
- Antes de pedir revisión: `test`, `lint` y `assembleDebug` en verde.

Nunca agregues trabajo de otra HU. Si HU-01/HU-02 todavía no existen, tu pantalla debe
funcionar y mostrar un error entendible cuando dependa de la sesión o de la API.

## 2. Estructura de una feature

Una feature es un dominio de negocio con el mismo nombre que en Flutter: `auth`,
`profile`, `orders`, `explore`, `tracking`.

```text
feature/<dominio>/
├── ui/       # *Screen.kt, *ViewModel.kt, *UiState.kt
├── domain/   # modelos, casos de uso y contratos (interface)
└── data/     # API, DTOs, mappers, implementación y módulo Hilt
```

Flujo: `ui → domain ← data`.

- `domain` es Kotlin puro: sin Android, sin Retrofit, sin Hilt.
- `ui` nunca importa DTOs ni clases de `data`.
- Lo transversal va en `core/`; lo que solo usa una feature, en su feature.

## 3. Hilt: todo se inyecta

Reglas:

- `@HiltAndroidApp` ya está en `ReciclaTruApp`.
- La actividad o fragmento que hospeda Compose lleva `@AndroidEntryPoint` (ya está en
  `MainActivity`).
- Cada ViewModel lleva `@HiltViewModel` y recibe casos de uso por constructor:
  `@Inject constructor(...)`.
- El binding interfaz ↔ implementación y los `@Provides` de la feature van en
  `feature/<dominio>/data/<Feature>Module.kt`.
- Dispatchers y alcances globales: usa `@IoDispatcher` y `@ApplicationScope` de
  `core/di/Qualifiers.kt`. No uses `Dispatchers.IO` suelto en un repositorio.
- **Prohibido** crear repositorios, Retrofit, OkHttp, el Geocoder o cualquier dependencia
  a mano dentro de Compose o del ViewModel. Si falta un binding, se agrega al módulo.

Ejemplo real: `feature/profile/data/ProfileModule.kt`.

## 4. Consumir la API

- Un solo cliente HTTP: `core/network/NetworkModule` (OkHttp + Retrofit + Gson). La URL
  base sale de `BuildConfig.API_BASE_URL`; no la repitas.
- El token lo agrega `AuthInterceptor` desde `SessionStore`. La sesión la crean y borran
  HU-01/HU-02.
- Toda llamada se envuelve con `apiCall { ... }`, que devuelve `Result<T>` y traduce
  `IOException`, timeout y códigos HTTP a `NetworkException` con un mensaje en español.
- En el ViewModel, muestra el error con `error.toUserMessage(fallback = "...")`.
- Los DTOs usan `@SerializedName` en **todos** los campos y viven en `data/`; los modelos
  de dominio no saben que existe Gson. El mapper DTO ↔ dominio va en un archivo aparte
  (ver `PickupLocationMapper.kt`).

Si el contrato OpenAPI no existe todavía, **no inventes** endpoints ni campos: usa la
operación del modelo de datos, déjala documentada como provisional en el `ProfileApi` y
avisa en el PR.

## 5. UI: MVVM sin lógica en la pantalla

Patrón obligatorio:

1. `*UiState.kt`: `data class` inmutable con todo lo que la pantalla dibuja, incluidos
   los indicadores (`saving`, `searching`…) y los errores por campo.
2. `*ViewModel.kt`: `@HiltViewModel`, un `MutableStateFlow<UiState>` privado y un
   `StateFlow` público. Recibe eventos (`onSave`, `onAddressChange`…), llama casos de uso
   y actualiza el estado. Nunca conoce Compose.
3. `*Screen.kt`: recoge el estado con
   `collectAsStateWithLifecycle()` y llama `viewModel` con `hiltViewModel()`. Solo dibuja
   y envía eventos; no hace red ni cálculos de negocio.

Ejemplo completo: `feature/profile/ui/` (HU-04).

Navegación:

- Las rutas viven en `core/navigation/AppRoutes.kt`.
- Se registran en `AppNavHost`, no dentro de la pantalla.
- Las guardas por sesión/rol se agregan cuando exista HU-01/HU-02. La app puede redirigir
  para mejorar la UX, pero el backend sigue validando permisos.

Búsqueda de direcciones:

- Usa `RtSearchField`/`RtMapPicker` con sugerencias; no armes una lista de sugerencias en la
  pantalla.
- El debounce de 300 ms, la consulta al Geocoder y el estado `searching` viven en el
  ViewModel. El componente solo muestra hasta cuatro sugerencias y avisa cuál se eligió.
- El Geocoder se implementa en `data/` detrás de un contrato de `domain/`; el componente y
  la pantalla no conocen Android.

## 6. Sistema de diseño: úsalo, no lo reemplaces

La única fuente es el sistema publicado:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

Rutas locales: `ui/designsystem/{theme,atoms,molecules,organisms}` y su
[README](app/src/main/java/pe/edu/upn/reciclatru/ui/designsystem/README.md), con la tabla
**Componentes por HU** y el estado de cada componente.

Reglas que más se incumplen:

- **Si el sistema tiene el componente, se usa.** No se rearma con átomos, no se copia y no
  se crea una variante local. Una tarjeta de pedido es `RtOrderCard`, no una `Card` con
  chips y textos.
- **Si falta algo**, se propone primero en el sistema y se implementa en Android y Flutter
  con el mismo nombre. Mientras no esté aprobado, no se resuelve con un widget suelto en
  la pantalla.
- Pantalla = `feature/*/ui`; los componentes reutilizables = `ui/designsystem`.
- Los componentes `Rt*` son stateless: reciben datos y callbacks, nunca ViewModels,
  repositorios ni modelos de dominio.
- Nada de `Color(0x…)`, `16.dp` ni tamaños sueltos: solo tokens de
  `ui/designsystem/theme`.
- Cada componente nuevo del sistema lleva `@Preview` con los mismos estados que su vista
  previa, y su demo en el catálogo de debug.
- Íconos: `Icons.Rounded.*` (Material Symbols Rounded), el mismo nombre que en Flutter.

### Componentes por HU

Antes de maquetar, busca tu HU en la tabla del README del sistema. Por ejemplo, HU-04 usa
`RtMapPicker`, `RtSearchField`, `RtTextField`, `RtErrorState`, `RtInlineAlert`, `RtButton`
y `RtActionBar`. Para HU-01, la tabla indica `RtButton`, `RtTextField`, `RtSpinner`,
`RtIconButton`, `RtRoleSelector` y `RtInlineAlert`. No agregues otros.

### Estados y textos

- Toda lista o detalle tiene carga (`RtSpinner`), vacío (`RtEmptyState`) y error con
  Reintentar (`RtErrorState`). Con datos previos, el error va en `RtSnackbar`.
- Una sola acción primaria por pantalla; en el pie, `RtActionBar`.
- El color nunca va solo: los estados llevan ícono y texto.
- Los textos de diálogos, vacíos y errores salen de las guías de `RtConfirmDialog`,
  `RtEmptyState`, `RtInlineAlert` y `RtErrorState`, y se usan tal cual.
- Se tutea, sin emojis y sin "exitosamente".
- Todo lo tocable mide al menos 48 dp; el texto de contenido, al menos 16 (usa
  `bodyLarge` o `RtTextStyles.bodyStrong`).

### `testTag` para tests de UI

La pantalla aplica el tag al componente, no el componente por su cuenta:

```kotlin
RtTextField(
    label = "Dirección",
    value = state.address,
    onValueChange = onAddressChange,
    modifier = Modifier.testTag("input_direccion"),
)
```

Nombres usados en HU-04: `map_punto_recojo`, `input_direccion`, `input_distrito`,
`btn_guardar_ubicacion`, `btn_listo_ubicacion`, `alert_ubicacion_info`. Sigue ese estilo:
qué es + para qué.

## 7. Tests

La app usa tests de ViewModel y mappers en JVM. **Nunca** llames a la API real desde un
test.

- `MainDispatcherRule` (`app/src/test/.../MainDispatcherRule.kt`) reemplaza
  `Dispatchers.Main`.
- Escribe dobles simples que implementen los contratos de `domain` (ver
  `LocationViewModelTest`).
- `runTest(mainDispatcherRule.testDispatcher)` + `advanceUntilIdle()` para resolver las
  corrutinas del `viewModelScope`.

Qué probar como mínimo en una HU:

1. Estado inicial.
2. Camino feliz (dato que llega, pantalla que avanza).
3. Error de la API (mensaje visible y sin perder lo escrito).
4. Validaciones propias de la pantalla (campos vacíos, etc.).
5. Que no se llame al repositorio cuando falta un dato obligatorio.

Para tests de UI, usa repositorios falsos con Hilt (`@TestInstallIn` /
`HiltTestApplication`), nunca la API real. La dependencia `hilt-android-testing` ya está
declarada; el primer test de UI debe declarar el runner de Hilt en `defaultConfig`.

## 8. Ejemplo de referencia: HU-04

Copia el patrón, no el código:

| Capa | Archivo | Qué mirar |
| --- | --- | --- |
| dominio | `feature/profile/domain/GeoPoint.kt` | modelo puro con validación |
| dominio | `feature/profile/domain/ProfileRepository.kt` | contrato sin Android |
| dominio | `feature/profile/domain/SavePickupLocationUseCase.kt` | caso de uso inyectable |
| datos | `feature/profile/data/ProfileApi.kt` | endpoint y DTOs |
| datos | `feature/profile/data/ProfileRepositoryImpl.kt` | sesión + `apiCall` + mapper |
| datos | `feature/profile/data/ProfileModule.kt` | bindings Hilt |
| UI | `feature/profile/ui/LocationUiState.kt` | estado inmutable |
| UI | `feature/profile/ui/LocationViewModel.kt` | eventos y errores |
| UI | `feature/profile/ui/LocationScreen.kt` | componentes del sistema + `testTag` |
| tests | `feature/profile/ui/LocationViewModelTest.kt` | dobles + estados |
| tests | `feature/profile/data/PickupLocationMapperTest.kt` | DTO ↔ dominio |

## 9. Checklist antes del PR

- [ ] La HU está completa: pantalla + API + tests; sin trabajo de otras HU.
- [ ] Rama `feature/SCRUM-XX-...` y commits `feat|fix|test|docs(...): SCRUM-XX ...`.
- [ ] `.\gradlew.bat test lint assembleDebug` en verde.
- [ ] Sin componentes nuevos fuera de `ui/designsystem` ni estilos sueltos.
- [ ] Estados de carga, vacío y error implementados con los componentes del sistema.
- [ ] `testTag` en los controles que probará el test de UI.
- [ ] Tests con dobles; ninguna llamada real a la API.
- [ ] Sin secretos, sin URLs duplicadas, sin dependencias nuevas sin justificar.
- [ ] Textos en español, tuteo, sin emojis ni "exitosamente".
- [ ] Si cambió una decisión de arquitectura, se actualizó el C4 y el `AGENTS.md`.
