# ReciclaTru API

API REST del Release 1 de ReciclaTru: conecta vecinos y recicladores de Huanchaco / Trujillo. Implementa el contrato `reciclatru-openapi-v1.yaml`.

- **Runtime:** Node.js 24 LTS, NestJS 12 (ESM), TypeScript 6
- **Base de datos:** PostgreSQL en Neon (esquema y migraciones en el repositorio `reciclatru-db`, con Flyway)
- **Fotos:** bucket privado S3-compatible (Neon Object Storage), URLs firmadas de 5 minutos
- **Despliegue:** Render (Blueprint `render.yaml`)

## Arquitectura

Módulos por dominio BIAN, cada uno en capas **controller → service → repository**:

```
src/
  config/                    variables de entorno validadas y configuración tipada
  database/                  conexión (TypeORM como pool) y transacciones (`Database`)
  common/                    errores, filtro problem+json, correlation id, seguridad, paginación, pipes
  health/                    GET /api/v1/health
  modules/
    party-data-management/   registro, perfil, ubicación (controller PartyController)
    location-data-management/ punto de recojo (instantáneas inmutables, RN18)
    party-authentication/    login JWT, guards globales (JWT y roles)
    photo-storage/           puerto PhotoStorage + implementación S3
    servicing-order/         pedidos, aceptaciones, fotos, listas paginadas
```

Principios aplicados:

- **Controller** solo traduce HTTP; **service** concentra reglas de negocio; **repository** es el único que escribe SQL.
- Reglas puras (`OrderRules`) separadas de la infraestructura: se prueban sin base de datos.
- `PhotoStorage` es una clase abstracta (puerto): cambiar de Neon a R2 o Azure no toca el dominio (DIP).
- La integridad crítica vive también en la base (triggers, índices parciales, `confirm_pickup_order`), no solo en el código.

## Requisitos

Node 24 (`nvm use`), PostgreSQL 13+ con las migraciones de `reciclatru-db` aplicadas.

## Puesta en marcha local

```bash
cp .env.example .env        # completar JWT_SECRET, PHONE_ENCRYPTION_KEY, DATABASE_URL, S3_*
npm ci
npm run start:dev           # http://localhost:3000/api/v1/health
```

Generar secretos: `openssl rand -base64 48` (JWT) y `openssl rand -base64 32` (clave AES del celular).

## Variables de entorno

| Variable | Obligatoria | Defecto | Descripción |
|---|---|---|---|
| `DATABASE_URL` | sí | | URL de Neon **con pooler** |
| `DATABASE_SSL` | no | `true` | `false` solo en local |
| `DATABASE_POOL_MAX` | no | `10` | Conexiones máximas |
| `JWT_SECRET` | sí | | Mínimo 32 caracteres |
| `JWT_EXPIRES_IN_DAYS` | no | `30` | Duración del token (sin refresh token en el Release 1) |
| `BCRYPT_ROUNDS` | no | `10` | Costo de bcrypt |
| `PHONE_ENCRYPTION_KEY` | sí | | Clave AES-256 de 32 bytes en base64 |
| `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY_ID`, `S3_SECRET_ACCESS_KEY` | sí | | Bucket privado |
| `S3_FORCE_PATH_STYLE` | no | `false` | **Poner `true` con Neon**: su certificado no cubre `bucket.host` (error `ERR_TLS_CERT_ALTNAME_INVALID`) |
| `PHOTO_URL_TTL_SECONDS` | no | `300` | Vida de la URL firmada |
| `PHOTO_MAX_BYTES` | no | `2097152` | Tamaño máximo de una foto |
| `ERROR_TYPE_BASE_URL` | no | | Base del campo `type` de los errores |
| `LOG_LEVEL` | no | `info` | `debug`, `info`, `warn` o `error` |
| `LOG_FORMAT` | no | `json` | `json` (producción) o `text` (local) |
| `OTEL_ENABLED` | no | `false` | `true` envía logs y trazas por OTLP |
| `OTEL_SERVICE_NAME` | no | `reciclatru-api` | Nombre del servicio en Grafana |
| `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_EXPORTER_OTLP_PROTOCOL`, `OTEL_EXPORTER_OTLP_HEADERS` | si `OTEL_ENABLED=true` | | Estándar OpenTelemetry; los entrega Grafana Cloud |

Si falta una variable obligatoria, la API no arranca y lista todas las que faltan.

## Decisiones que conviene conocer

**Celular ofuscado.** `PUT /party-data-management/{id}/update` guarda `987654321` como `987****21` en `user_profile.phone` y, además, el valor real cifrado con AES-256-GCM en `user_profile.phone_encrypted`. El cifrado es necesario porque la app debe mostrar el teléfono real a la contraparte autorizada (HU-12 y HU-13). El dueño del perfil recibe su número real; los demás solo lo ven según las reglas RN13. Si se pierde `PHONE_ENCRYPTION_KEY`, los celulares guardados no se pueden recuperar.

**Referencia del vecino** vive en `user_profile.reference_note` (se lee en vivo); la ubicación sigue siendo una instantánea inmutable.

**Trazabilidad.** Cada respuesta lleva `x-correlation-id`. Si la app envía uno válido (`[A-Za-z0-9._:-]`, hasta 100 caracteres) se conserva; si no, se genera un UUID. Aparece también en los errores (`correlationId`) y en el log de cada petición.

**Errores** (`application/problem+json`, RFC 7807): `type`, `title`, `status`, `code`, `detail`, `instance`, `errors[]`, `correlationId`.

| HTTP | Cuándo | Códigos |
|---|---|---|
| 400 | Datos mal formados, cursor inválido | `VALIDATION_ERROR` |
| 401 | Token ausente, inválido o vencido; credenciales incorrectas | `UNAUTHENTICATED` |
| 403 | Rol no permitido o pedido ajeno | `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER` |
| 404 | No existe (o id mal formado) | `NOT_FOUND` |
| 409 | Estado incompatible o concurrencia | `EMAIL_ALREADY_REGISTERED`, `ORDER_NOT_AVAILABLE`, `ORDER_NOT_EDITABLE`, `ORDER_NOT_CANCELLABLE`, `ORDER_NOT_PENDING`, `ORDER_NOT_CONFIRMED`, `CONCURRENT_UPDATE` |
| 413 | Foto demasiado grande | `PHOTO_TOO_LARGE` |
| 422 | Regla de negocio | `PROFILE_INCOMPLETE`, `INVALID_ITEMS`, `INVALID_WEIGHT`, `PHOTO_LIMIT_REACHED`, `PHOTO_INVALID_TYPE` |
| 500 | Error interno (sin detalles hacia fuera) | `INTERNAL_ERROR` |

**Paginación.** Cursor opaco (base64url) por keyset: estable aunque lleguen pedidos nuevos mientras se pagina. `limit` de 1 a 50 (20 por defecto). Un cursor alterado responde 400.

**Logs y observabilidad.** Cada línea es un JSON con `timestamp`, `level`, `context`, `message`, `correlationId`, `userId` y `role` (cuando hay sesión), más `traceId` y `spanId` si OpenTelemetry está activo. Se escribe:

- Una línea `http.request` por petición, al terminar: método, **plantilla** de la ruta (`/api/v1/servicing-order/:orderId/retrieval`, nunca la URL real, así no se filtran ids ni cursores), status, `durationMs` y `errorCode`. Incluye las rechazadas (401, 403, 404). El nivel sale del resultado: `info` 2xx/3xx, `warn` 4xx, `error` 5xx. `/health` va en `debug`.
- Eventos de negocio con el campo `event`: `user.registered`, `auth.login.succeeded`, `auth.login.failed`, `profile.updated`, `location.updated`, `order.created`, `order.updated`, `order.cancelled`, `order.accepted`, `order.confirmed`, `order.collected`, `order.photo.uploaded`, `order.photo.removed`.
- Nunca se registran contraseñas, tokens, celulares, correos, coordenadas ni direcciones: el logger enmascara las claves sensibles (`password`, `token`, `phone`, `email`...) y los eventos solo llevan ids y conteos.

Para agregar un evento: `this.logger.log({ event: 'order.algo', orderId })` con el `Logger` de Nest; el `correlationId` se añade solo.

**OpenTelemetry (Grafana Cloud).** Con `OTEL_ENABLED=true`, `src/instrumentation.ts` (cargado con `node --import` en `npm run start:prod`) envía por OTLP/HTTP las trazas (HTTP, Express y cada consulta SQL de `pg`, sin `/health`) y los mismos logs. Cada traza lleva el atributo `app.correlation_id`. Si el token o el endpoint son incorrectos, el aviso aparece en la consola del servicio. Pasos en Grafana Cloud: *Portal → tu stack → Details → OpenTelemetry → Configure* y copiar las tres variables `OTEL_EXPORTER_OTLP_*`. Probar en local: `npm run build`, poner las variables en `.env` y `npm run start:prod`; luego, en Grafana, *Explore → Loki* (`{service_name="reciclatru-api"}`) y *Explore → Tempo*.

**`/health`** está bajo el prefijo: `GET /api/v1/health` (público, comprueba la base de datos y responde 503 si no responde).

## Scripts

| Comando | Qué hace |
|---|---|
| `npm run start:dev` | Desarrollo con recarga |
| `npm run build` / `npm run start:prod` | Compila y ejecuta `dist/main.js` con OpenTelemetry cargado (lee `.env`) |
| `npm test` | Pruebas unitarias (Jest, sin base de datos) |
| `npm run test:integration` | Flujo completo contra PostgreSQL (`TEST_DATABASE_URL`, por defecto `postgres://postgres@localhost:5433/reciclatru_test`) con un bucket simulado |
| `npm run test:cov` | Cobertura |
| `npm run lint`, `npm run typecheck`, `npm run format` | Calidad de código |

Pruebas de integración: aplicar antes `reciclatru-db/migrations/V*.sql` en una base vacía.

## Despliegue

1. **Neon:** proyecto `reciclatru` (us-east-2), bucket `photos` en **Private**, credenciales de almacenamiento y URL **con pooler** para la API. Flyway usa la URL directa: `flyway migrate` desde `reciclatru-db` antes de cada despliegue que cambie el esquema.
2. **Render:** *New → Blueprint* sobre este repositorio (`render.yaml`). Cargar los secretos marcados `sync: false` (`DATABASE_URL`, `PHONE_ENCRYPTION_KEY`, `S3_*`). `JWT_SECRET` lo genera Render.
3. Verificar `https://<servicio>.onrender.com/api/v1/health`.

El plan gratuito de Render duerme el servicio tras un rato sin tráfico: la primera petición puede tardar. Revisar las condiciones vigentes de Render y Neon antes de depender de ellas.
