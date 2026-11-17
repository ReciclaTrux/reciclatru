# reciclatru-perf-tests

Pruebas de **carga y estrés** del API de ReciclaTru con [k6](https://k6.io). Repositorio independiente: solo necesita la
URL del API, no depende de su código. Contrato de referencia: `reciclatru-openapi-v1.yaml` (repositorio del API).

> ⚠️ **Nunca apuntes esto a producción.** Crea usuarios y pedidos de prueba y genera mucha carga. Usa tu PC con una
> rama de Neon de pruebas, o un servicio de Render aparte con su propia base.

## Requisitos

1. **k6** (no se instala con npm):
   - Windows: `winget install k6` (o `choco install k6`)
   - macOS: `brew install k6` · Docker: `grafana/k6`
2. El API de pruebas levantado **con el límite de solicitudes desactivado**: `RATE_LIMIT_ENABLED=false`.
   Todo el tráfico sale de una sola IP; con el límite activo casi todo sería 429 y no mediría rendimiento.
   El límite se prueba aparte (ver más abajo).

## Uso

```bash
npm run smoke                                   # 1 usuario por rol, 30 s: ¿funciona todo?
npm run load                                    # carga esperada, ~7 min
npm run stress -- -e VUS=150                    # sube por escalones hasta el límite
npm run spike                                   # pico brusco y recuperación
npm run soak -- -e VUS=10 -e DURATION=1h        # resistencia
```

Sin npm también sirve: `k6 run tests/load.test.js -e BASE_URL=https://mi-api-de-pruebas.onrender.com`.
Al terminar cada prueba se genera un **reporte HTML** en `results/<prueba>.html` (ábrelo con doble clic en el navegador; no necesita internet) y, con `--summary-export`, los números en `results/<prueba>.json`. La terminal muestra además un resumen corto de metas.

### Variables (`-e NOMBRE=valor`)

| Variable | Por defecto | Qué hace |
|---|---|---|
| `BASE_URL` | `http://localhost:3000` | URL del API (sin `/api/v1`) |
| `VUS` | `20` | Usuarios virtuales en el punto más alto (70 % vecinos, 30 % recicladores) |
| `USERS` | `10` | Usuarios de prueba por rol que se crean al empezar |
| `THINK` | `1` | Multiplicador de las pausas entre acciones (`0` = máxima presión) |
| `LOGIN_RATE` | `0.05` | Fracción de iteraciones con login real (bcrypt, el paso más caro) |
| `MAX_MS` | `2000` | Máximo permitido por petición (requisito del sistema: toda respuesta en ≤ 2 s) |
| `P95_MS` | igual a `MAX_MS` | Meta del percentil 95 (con el máximo en 2 s, el p95 ya queda cubierto) |
| `LOGIN_MAX_MS` | igual a `MAX_MS` | Máximo permitido para el login |
| `MAX_ERROR_RATE` | `0.01` | Tasa máxima de errores |
| `DURATION` | `30m` | Solo `soak`: tiempo sostenido |
| `TEST_PASSWORD` | `Reciclatru2026` | Contraseña de los usuarios de prueba |

## Qué ejecuta

Al empezar (`setup`) despierta el API (`/health`, hasta 60 s por el arranque en frío de Render), registra `USERS` vecinos y
`USERS` recicladores con correos únicos por corrida (`perf.<timestamp>.n0@example.com`), completa sus perfiles, fija la
ubicación de los vecinos y deja 3 pedidos `PENDING` por vecino.

| Rol | Acciones por iteración |
|---|---|
| Vecino | mis pedidos activos · (30 %) publicar pedido + ver detalle · (20 %) historial · (5 %) login |
| Reciclador | pedidos disponibles por cercanía · (30 %) aceptar uno (`409` cuenta como válido) · mis aceptados · (5 %) login |

Las métricas se etiquetan por nombre de acción (`login`, `orders_list_active`, `order_create`, `availability`,
`order_accept`…) y por fase: las metas solo miran `phase:run`, no la preparación.

## Tipos de prueba

| Prueba | Forma | Para qué |
|---|---|---|
| `smoke` | 1 + 1 usuarios, 30 s | Validar scripts y API |
| `load` | sube 1 min · sostiene 5 min · baja | Latencia con el tráfico esperado |
| `stress` | escalones 20→40→60→80→100 % de `VUS` | Encontrar dónde se degrada; **aborta** si >20 % falla |
| `spike` | 10 % → 100 % en 10 s → 10 % | ¿Resiste y se recupera? |
| `soak` | carga constante `DURATION` | Fugas de memoria o conexiones |

## Cómo leer el resultado

- **p95/p99** de `http_req_duration`, no el promedio. Meta por defecto (requisito del sistema): **ninguna petición supera 2 s** (`max`).
- **`http_req_failed`** < 1 %. **`checks`** > 99 %.
- Si una meta no se cumple, k6 termina con código distinto de 0 (útil en CI).
- Busca el escalón del `stress` donde la latencia se dispara: suele ser el pool de conexiones a Neon o la CPU (bcrypt).

Mientras corre, abre el dashboard **ReciclaTru API - Monitoreo** en Grafana para ver 5xx, 4xx y latencia por ruta del lado del
servidor. Con carga, los logs y trazas crecen mucho: considera `LOG_LEVEL=warn` en el API de pruebas.

## Probar el límite de solicitudes (aparte)

Con `RATE_LIMIT_ENABLED=true`, un script corto que haga 15 logins seguidos con credenciales incorrectas debe terminar en
`429` con `Retry-After`. No va en estas pruebas porque falsearía las métricas de rendimiento.

## Datos que quedan

Cada corrida deja usuarios y pedidos de prueba (`perf.*@example.com`). Es una base de pruebas: bórrala o restaura la rama de
Neon cuando quieras.

## Estructura

```
src/config.js      variables y metas (thresholds)
src/report.js      reporte HTML y resumen de terminal (handleSummary)
src/http.js        llamada al API con etiquetas y manejo de códigos esperados
src/setup.js       preparación: despertar API, crear usuarios y pedidos
src/data.js        datos aleatorios
src/flows/         qué hace un vecino y un reciclador
src/scenarios.js   reparto 70/30 y etapas
tests/*.test.js    smoke, load, stress, spike, soak
.github/workflows  ejecución manual desde GitHub Actions
```

## Relación con las pruebas Karate

Este repositorio mide **rendimiento**. Las pruebas funcionales automáticas (Karate) irán en otro repositorio y validan
**comportamiento**: códigos, cuerpos y reglas de negocio contra el contrato OpenAPI. Los dos comparten solo la URL del API y
el contrato; no comparten código.