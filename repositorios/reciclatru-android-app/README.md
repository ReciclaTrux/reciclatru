# ReciclaTru — Android

Cliente principal de **ReciclaTru**, que conecta a vecinos de Trujillo con recicladores
(ODS 12). Kotlin + Jetpack Compose + Material 3, arquitectura MVVM por feature e
inyección de dependencias con Hilt. Paquete base: `pe.edu.upn.reciclatru`.

La base de la app está lista y **HU-04 (punto de recojo) implementada como ejemplo** de
corte vertical, con tests. HU-01, HU-02 y HU-03 todavía no se tocan.

## Estructura

```text
app/src/main/java/pe/edu/upn/reciclatru/
├── core/
│   ├── network/       # OkHttp, Retrofit, interceptor de token, errores
│   ├── session/       # Session, roles y SessionStore (DataStore)
│   ├── navigation/    # AppRoutes y AppNavHost
│   └── di/            # módulos Hilt globales y calificadores
├── ui/designsystem/   # sistema de diseño: theme/ atoms/ molecules/ organisms/
├── feature/
│   ├── auth/          # HU-01 y HU-02 (pendiente)
│   ├── profile/       # HU-03 y HU-04 (HU-04 lista como ejemplo)
│   ├── orders/        # HU-05 a HU-09
│   ├── explore/       # HU-10 a HU-12
│   └── tracking/      # HU-13 a HU-16
├── ReciclaTruApp.kt   # @HiltAndroidApp
└── MainActivity.kt    # @AndroidEntryPoint → AppNavHost
```

Cada feature tiene `ui/` (pantalla, ViewModel y estado), `domain/` (modelos, casos de uso
y contratos, Kotlin puro) y `data/` (Retrofit, DTOs, mappers, implementación y módulo
Hilt). El flujo de dependencias es `ui → domain ← data`.

## Antes de hacer una HU

Lee **[`GUIA-HU.md`](GUIA-HU.md)**. Explica cómo se arma una HU completa, cómo usar Hilt,
cómo consumir la API y —lo más importante— cómo usar el sistema de diseño sin inventar
componentes. También tienes las reglas del repo en [`AGENTS.md`](AGENTS.md) y el flujo
del monorepo en `../../AGENTS.md`.

El sistema de diseño es la única fuente de estilos y componentes:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

Estado de cada componente y tabla **Componentes por HU**:
`app/src/main/java/pe/edu/upn/reciclatru/ui/designsystem/README.md#componentes-por-hu`.

## Configuración

- **API**: `BuildConfig.API_BASE_URL` en `app/build.gradle.kts`
  (`https://reciclatru.onrender.com/api/v1/`, la API publicada en Render). Ningún
  repositorio repite la URL.
- **Sesión**: `core/session/SessionStore` guarda el token en DataStore y
  `AuthInterceptor` lo agrega a cada petición. HU-01/HU-02 son las que crean y borran la
  sesión.
- **Hilt + KSP**: el proyecto usa AGP 9 con Kotlin integrado. KSP registra sus fuentes con
  el DSL de Kotlin, por eso `gradle.properties` tiene
  `android.disallowKotlinSourceSets=false` (ajuste indicado por AGP). Se quita cuando KSP
  deje de necesitarlo.
- **Debug**: `app/src/debug/AndroidManifest.xml` permite HTTP sin TLS solo para el
  backend local. En release no.

## Conexión con el backend (RT-50)

Al abrir la app, la pantalla de inicio llama a `GET /api/v1/health` y muestra
**"Conectado"** si el backend responde `{"status":"UP"}`. Si falla, muestra el error con
**Reintentar**. El código vive en `core/health/` (`data`, `domain` y `ui`).

### Red y permisos

- **Permiso de internet**: `android.permission.INTERNET` (y `ACCESS_NETWORK_STATE`) en
  `app/src/main/AndroidManifest.xml`.
- **URL base**: `BuildConfig.API_BASE_URL` en `app/build.gradle.kts` apunta a la API
  publicada: `https://reciclatru.onrender.com/api/v1/`. No hace falta levantar la API en
  local, y funciona igual en emulador y en dispositivo físico.
- **HTTPS**: la API publicada usa TLS, así que no se necesita permitir HTTP sin cifrar.
- **Backend local (opcional)**: para apuntar a una API en la PC, la URL es
  `http://10.0.2.2:3000/api/v1/` (en el emulador, `localhost` es el propio emulador y
  `10.0.2.2` es la PC). Como es HTTP, solo funciona en debug, donde
  `app/src/debug/AndroidManifest.xml` activa `usesCleartextTraffic`.
- **Render en plan gratuito**: tras un rato sin uso el servidor se duerme y la primera
  petición puede tardar o vencer el tiempo de espera; en ese caso, usar Reintentar.

### Cómo probarlo

1. Abrir `https://reciclatru.onrender.com/api/v1/health` en el navegador: debe
   responder `{"status":"UP",...}`.
2. Ejecutar la app en el emulador: el inicio debe mostrar "Conectado".
3. Con el emulador en modo avión, debe mostrar "No tienes conexión..." y el botón
   Reintentar.

## HU-04 como referencia

La pantalla de punto de recojo es el ejemplo de corte vertical:

| Capa | Archivo |
| --- | --- |
| dominio | `feature/profile/domain/{GeoPoint,PickupLocation,GeocodingRepository,ProfileRepository}` y casos de uso |
| datos | `feature/profile/data/{ProfileApi,AndroidGeocoderDataSource,GeocodingRepositoryImpl,ProfileRepositoryImpl,ProfileModule}` |
| UI | `feature/profile/ui/{LocationScreen,LocationViewModel,LocationUiState}` |
| tests | `app/src/test/java/pe/edu/upn/reciclatru/feature/profile/` |

- El mapa real es `RtMapPicker` (MapLibre + estilo **positron** de OpenFreeMap, sin API
  key). El geocoder es el de Android (`android.location.Geocoder`); en Flutter será el
  paquete `geocoding`.
- El buscador tiene autocomplete con debounce de 300 ms: hasta cuatro sugerencias, filas de
  48 dp y la cámara se mueve al elegir una. La consulta se sesga a Trujillo.
- La pantalla no usa GPS ni pide permiso de ubicación.
- Guardar requiere sesión: sin login, la pantalla muestra "Inicia sesión para guardar tu
  ubicación.". Es el comportamiento esperado hasta que HU-01/HU-02 existan.

## Compilar y probar

En Windows usar `.\gradlew.bat`:

```bash
./gradlew assembleDebug        # APK de debug
./gradlew test                 # tests unitarios (ViewModel y mappers)
./gradlew lint                 # Android Lint
./gradlew connectedAndroidTest # UI con emulador/dispositivo
```

El catálogo del sistema de diseño se abre solo en debug:
`pe.edu.upn.reciclatru/.ui.catalog.RtCatalogActivity`.

## Pendientes conocidos

- **HU-01/HU-02**: registro, ingreso, guardas de navegación y creación de la sesión. El
  inicio de `AppNavHost` es un marcador hasta que existan.
- **Contrato OpenAPI**: `reciclatru-openapi` todavía está vacío. La operación de guardar
  ubicación es provisional; se ajusta en `ProfileApi` y su mapper.
- **Lectura de la ubicación guardada**: no hay operación de `GET` en el contrato, así que
  el mapa siempre parte del centro de Trujillo.
- **Miniatura del mapa** en el paso de dirección: `RtMapPicker` tiene altura fija y no
  admite modo compacto. Hay que proponerlo al sistema de diseño antes de implementarlo.
- **Flutter**: el Lote 2 del sistema de diseño y la paridad de HU-04 (mapa con
  `maplibre_gl`, geocoder con `geocoding`) siguen pendientes.

## Más información

- Guía para desarrollar una HU: [`GUIA-HU.md`](GUIA-HU.md).
- Reglas del proyecto y de arquitectura: [`AGENTS.md`](AGENTS.md).
- Plan del sistema de diseño: `../../PLAN-SISTEMA-DISENO.md`.
