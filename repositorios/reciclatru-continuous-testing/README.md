# reciclatru-karate-tests

Pruebas automáticas del API de ReciclaTru con [Karate](https://github.com/karatelabs/karate), cubriendo **happy path** y
**unhappy path** de cada endpoint. Repositorio independiente: solo necesita la URL del API (y su contrato, `reciclatru-openapi-v1.yaml`).

> ⚠️ **Nunca lo apuntes a producción.** Cada corrida crea usuarios (`karate.*@example.com`) y pedidos de prueba.
> Úsalo contra tu PC con una rama de Neon de pruebas, o un servicio de Render aparte con su propia base.

## Requisitos

- **Java 17+** y **Maven 3.9+**
- El API de pruebas levantado con:
  - `RATE_LIMIT_ENABLED=false` (todo el tráfico sale de una IP; con el límite activo la regresión recibiría 429).
    El límite se prueba aparte (suite `ratelimit`, ver abajo).
  - `CORS_ORIGINS` con el origen que uses en `-DcorsOrigin` (por defecto `http://localhost:5173`; con `http://localhost:*` basta).
  - Bucket de fotos configurado (S3/Neon). Si no lo hay, ejecuta con `-Dstorage=false`.
  - Opcional: `BCRYPT_ROUNDS=4` en local para que corra más rápido.

## Uso

```bash
# Regresión completa contra tu API local (http://localhost:3000/api/v1)
mvn test

# Contra otro entorno
mvn test -DbaseUrl=https://mi-api-de-pruebas.onrender.com/api/v1

# Solo humo (escenarios @smoke, ~1 min): útil tras cada despliegue
mvn test -Dsuite=smoke

# Sin lo que necesita el bucket de fotos
mvn test -Dstorage=false

# Límite de solicitudes (el API debe tener RATE_LIMIT_ENABLED=true; corre en 1 hilo)
mvn test -Dsuite=ratelimit

# Un solo feature o escenario (línea) con el jar de Karate, sin Maven: ver "Depuración"
```

| Propiedad | Por defecto | Qué hace |
|---|---|---|
| `-DbaseUrl` | `http://localhost:3000/api/v1` | URL del API (con `/api/v1`) |
| `-Dkarate.env` | `local` | Entorno de `karate-config.js` |
| `-Dsuite` | `regression` | `regression` · `smoke` · `ratelimit` |
| `-Dstorage` | `true` | `false` omite los escenarios `@storage` |
| `-Dthreads` | `4` | Features en paralelo (`1` por defecto con `ratelimit`; en Render gratuito, 2–4) |
| `-DcorsOrigin` | `http://localhost:5173` | Origen que el API permite por CORS |

Reportes: `target/karate-reports/karate-summary.html` (HTML), `target/karate-reports/*.xml` (JUnit) y `*.json` (Cucumber).

## Etiquetas

| Etiqueta | Significado |
|---|---|
| `@smoke` | Camino crítico (~12 escenarios): salud, registro, login, ciclo completo, publicar, editar, cancelar, aceptar, fotos |
| `@storage` | Necesita el bucket de fotos |
| `@cors` | Depende de `CORS_ORIGINS` del API |
| `@ratelimit` | Solo con el límite activo; nunca corre en `regression` |
| `@known-issue` | **Hallazgo documentado**: falla hoy porque el API se aparta del contrato. Fuera de la ejecución normal; quita la etiqueta cuando se corrija |
| `@ignore` | Helpers reutilizables (`common/`), no son pruebas |

## Qué cubre

Resultado de la última verificación: **207 escenarios de regresión + 4 de límite de solicitudes, todos en verde**, y 2 hallazgos `@known-issue`.

| Carpeta (`src/test/resources/reciclatru/`) | Endpoints | Happy path | Unhappy path |
|---|---|---|---|
| `health/` | `GET /health` | UP sin token, correlation id | — |
| `auth/registro` | `POST /party-data-management/initiation` | vecino/reciclador, correo normalizado | correo duplicado (también con mayúsculas), datos inválidos, campos faltantes, campos extra (asignación masiva) |
| `auth/login` | `POST /party-authentication/initiation` | JWT de 30 días, correo sin mayúsculas | contraseña mala y correo inexistente responden igual (sin enumerar cuentas), cuerpos inválidos |
| `auth/tokens` | todas las rutas protegidas | — | 401 sin token, token inventado, esquema Basic, firma alterada; 403 por rol |
| `party/perfil` | `GET/PUT /party-data-management/{id}/…` | perfil completo, edición parcial, alias `me` | celular/nombre/referencia inválidos, rol y correo inmutables, perfil ajeno → 404 |
| `location/ubicacion` | `PUT /location-data-management/me/pickup-point/update` | redondeo a 6 decimales, cambio solo afecta pedidos nuevos (RN18) | reciclador 403, coordenadas fuera de rango |
| `orders/crear-pedido` | `POST /servicing-order/initiation` | 1–10 ítems, pesos en el borde | 0/11 ítems, pesos inválidos, ítems mal formados, perfil incompleto, rol, campos extra |
| `orders/listar-pedidos` | `GET /servicing-order/retrieval` | aislamiento entre vecinos, orden, ACTIVE/HISTORY, paginación por cursor | tab/límite/cursor inválidos, rol |
| `orders/detalle-pedido` | `GET /servicing-order/{id}/retrieval` | dueño | ajeno 403, inexistente 404, id mal formado 404 |
| `orders/editar-pedido` | `PUT …/update` | edición con recálculo | validaciones, con aceptación/cancelado → 409, ajeno 403, rol, 404 |
| `orders/cancelar-pedido` | `PUT …/termination` | pendiente y confirmado, borrado lógico, reflejo en el reciclador | doble cancelación, recolectado, ajeno, rol, 404 |
| `orders/fotos` | `POST …/photo/initiation`, `GET …/retrieval`, `PUT …/termination` | subir, 302 a URL firmada, quitar | máx. 3, >2 MB, tipo falso, sin archivo, rol, ajeno, no editable, 404 |
| `recycler/disponibles` | `GET /servicing-order/availability/retrieval` | orden por fecha y por distancia, solo `PENDING`, `alreadyAccepted`, sin datos personales | rol, lat/lng/límite/cursor inválidos |
| `recycler/aceptar-pedido` | `POST …/acceptance/initiation`, `GET …/acceptance/retrieval` (ambos) | aceptar, idempotencia (doble toque), lista del vecino | rol, 404, cancelado/ya confirmado → 409, perfil incompleto, ajeno |
| `flows/confirmar-recolectar` | `PUT …/confirmation/execution`, `PUT …/fulfillment/execution` | elegir reciclador | doble elección, cancelado, uuid inválido, aceptación de otro pedido, rol, ajeno, recolectar sin confirmar/doble/cancelado |
| `flows/ciclo-completo` | todo el recorrido | publicar → 2 aceptan → elegir → recolectar, con la visibilidad de datos en cada paso | — |
| `privacy/privacidad` | `retrieval`, `availability` | solo el elegido con pedido `CONFIRMED` ve la dirección | sin aceptar, descartado, recolectado y listas no filtran datos del vecino |
| `security/cabeceras-y-errores` | transversal | helmet, `x-correlation-id`, CORS | 404 problem+json, JSON roto → 400, >50 KB → 413, errores sin trazas internas |
| `security/limite-solicitudes` | login, registro, general, `/health` | `/health` nunca se limita | 429 con `Retry-After` |

Cada respuesta de error se valida contra la forma RFC 7807 (`application/problem+json`) y su **código de negocio** (`problem(status, 'CODIGO')` en `karate-config.js`).

## Hallazgos (`@known-issue`)

Encontrados al escribir las pruebas contra el contrato `reciclatru-openapi-v1.yaml`:

1. **`Location` ausente en el 201 de `POST /servicing-order/initiation`.** El contrato lo promete; la API no lo envía.
2. **Los errores del lector de JSON no llevan `x-correlation-id`** (ni en la cabecera ni en `correlationId` del cuerpo): JSON mal formado (400) y cuerpo > 50 KB (413). Pasan antes del middleware de correlación, así que esos errores no se pueden rastrear en Grafana. El contrato dice que *toda* respuesta lo incluye.

Otras diferencias menores con los ejemplos del contrato (la respuesta de crear pedido omite algunos campos `null`) no se prueban para no fijar un detalle que el contrato muestra solo como ejemplo.

## Estructura

```
pom.xml
src/test/java/reciclatru/SuiteRunner.java   ejecuta los features en paralelo (suite, tags, hilos, reportes)
src/test/resources/
├── karate-config.js    baseUrl por entorno, problem(), hasHeader(), isAscending(), bearer()
├── headers.js          x-correlation-id "karate-<uuid>" en cada petición
├── fixtures/           pixel.png (válido), nota.txt (no es imagen)
└── reciclatru/
    ├── common/         helpers @ignore: create-user, create-order, accept-order, confirm-order, …
    ├── health/ auth/ party/ location/ orders/ recycler/ flows/ privacy/ security/
.github/workflows/karate.yml   ejecución manual, nocturna y tras cada despliegue del API
```

## Convenciones (aprendidas a golpes)

- **Datos propios por escenario.** Cada escenario crea sus pedidos con helpers; los usuarios se comparten dentro de un feature con
  `callonce`. Así se puede correr en paralelo y repetir sobre una base que ya tiene datos.
- **`callonce` cachea por (feature + argumentos).** Dos `callonce` con los mismos argumentos devuelven el *mismo* usuario;
  para obtener otro, pasa un argumento distinto (`alias: 'otro'`).
- **`* call` sin asignar filtra las variables del helper** al escenario (pisó un `chosen`). Por eso se escribe
  `* def done = call read(...)`.
- **Una expresión con `==` dentro de un `match`** se parte en el primer `==`; calcula antes con `* def` (ver los `karate.filter`).
- **Columnas de `Examples:` son variables.** No las nombres `lat`/`lng`/`token`: pisan variables del config.
- **Rastrear una corrida en Grafana:** todas las peticiones llevan `x-correlation-id: karate-<uuid>`.
  En Loki: `{service_name="reciclatru-api"} | correlationId=~"karate-.*"`; y el error de un escenario fallido trae su `correlationId`.
  Para fijar uno propio: `* def correlationId = 'mi-id'` antes de la petición.

## Cómo agregar un escenario

1. Elige el feature de su carpeta (o crea uno nuevo; se descubre solo).
2. Prepara datos con los helpers (`create-user`, `create-order`, `accept-order`, `confirm-order`, …).
3. Un happy path valida el **cuerpo exacto** cuando importa (`match response == {...}` detecta campos que se filtran); un unhappy
   path valida **status + `problem(status, 'CODIGO')`**.
4. Si descubres que el API se aparta del contrato, escribe la prueba como debería ser y márcala `@known-issue`.

## Depuración sin Maven

Con el jar independiente de Karate (descárgalo de las [releases](https://github.com/karatelabs/karate/releases)):

```bash
java -cp "karate-1.5.2.jar:src/test/resources" com.intuit.karate.Main \
     -T 4 -t "~@ignore" -t "~@ratelimit" -t "~@known-issue" -o target/run src/test/resources/reciclatru
# un feature, o una línea de escenario
java -cp "karate-1.5.2.jar:src/test/resources" com.intuit.karate.Main src/test/resources/reciclatru/orders/crear-pedido.feature:42
```

## Integración continua

`.github/workflows/karate.yml` corre la suite contra la URL de la variable de repositorio `API_BASE_URL`
(Settings → Secrets and variables → Actions → Variables), que debe ser un API **de pruebas**:

- **Manual:** Actions → karate → *Run workflow* (URL, suite y si hay bucket de fotos).
- **Nocturna:** 03:00 hora de Lima.
- **Tras cada despliegue del API:** el repositorio del API puede dispararla con
  `gh api repos/<usuario>/reciclatru-karate-tests/dispatches -f event_type=api-deployed -f 'client_payload[base_url]=https://…/api/v1'`.

Antes de la suite despierta el servicio con `/health` (Render gratuito se duerme). Los reportes quedan como artefacto de la corrida.

## Relación con las pruebas de carga

`reciclatru-perf-tests` (k6) mide **rendimiento**; este repositorio valida **comportamiento**. Comparten solo la URL del API y el contrato.
