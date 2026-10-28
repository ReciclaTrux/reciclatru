# ReciclaTru API v1.0.0

> Documentación generada desde `reciclatru-openapi-v1.yaml` (OpenAPI 3.0.3). El YAML es la fuente de verdad: si cambia, se regenera este archivo.

API de ReciclaTru (Release 1), alineada al Informe de Proyecto Final (UPN 2026-2) y a la nomenclatura BIAN.
Conecta vecinos con material reciclable y recicladores en Huanchaco / Trujillo.

**Decisiones del Release 1 (según el informe)**
- Un solo rol por cuenta: `NEIGHBOR` (vecino) o `RECYCLER` (reciclador). El rol y el correo no se pueden cambiar (RN02, RN03).
- Autenticación propia: contraseña con bcrypt y JWT de 30 días (`Authorization: Bearer <token>`). Cerrar sesión borra el token en la app (HU-02).
- Pedido con 1 a 10 ítems (descripción y peso en kg) y hasta 3 fotos opcionales (RN06, RN07).
- Varios recicladores pueden aceptar un pedido `PENDING`; el vecino confirma a uno solo (RN09, RN12).
- El vecino marca el pedido como `COLLECTED` (RN15). Los pedidos no se borran, solo se cancelan (RN16, RN17).
- Ubicación del vecino marcada en mapa (sin GPS); se copia a cada pedido al publicarse (RN18).
- Ubicación del reciclador: GPS aproximado enviado como parámetro, **no se guarda** (RN19).
- Privacidad: la dirección, la referencia y las coordenadas exactas solo las ve el reciclador elegido con el pedido `CONFIRMED` (RN13).
- Sin notificaciones push, chat, pagos ni precios (RN14). La app consulta con pull-to-refresh.
- Errores en `application/problem+json` (RFC 7807). Un cambio de estado concurrente responde `409 Conflict`.
- Fuera del Release 1 (Release 2): recuperar contraseña, verificar teléfono, catálogo de materiales, mapa y filtros del reciclador, avisos, vencimiento automático, tope de aceptaciones, cancelación automática, reabrir pedido y retirar aceptación.

**Convención BIAN usada**
- Ruta: `/{service-domain}/{cr-reference-id}/{behavior-qualifier}/{action-term}` en kebab-case; el *action term* va sustantivado (`initiation`, `retrieval`, `update`, `execution`, `termination`).
- Mapeo HTTP: Initiate → `POST`; Retrieve → `GET`; Update, Execute y Terminate → `PUT`.
- `cr-reference-id` es la referencia del Control Record (usuario, pedido). Las respuestas incluyen `crReferenceId` y `...InstanceRecord`.
- Los Service Domains son la correspondencia más cercana del landscape BIAN (bancario); no existe un dominio oficial de reciclaje. Cada operación declara su equivalencia en `x-bian`.
- `/health` queda fuera de BIAN.

**Trazabilidad y datos sensibles**
- Cada respuesta incluye el encabezado `x-correlation-id`. Si la app envía uno (hasta 100 caracteres: letras, números y `._:-`) se conserva; si no, el servidor genera un UUID. Los errores lo repiten en `correlationId`.
- El celular se guarda **ofuscado** en la base de datos (`987654321` queda como `987****21`) y cifrado aparte. La API devuelve el número real solo al dueño del perfil y a la contraparte autorizada (HU-12, HU-13).
- Las listas usan paginación por **cursor opaco**: la app envía de vuelta `nextCursor` sin interpretarlo. Un cursor alterado responde `400`.

## Servidores

| Entorno | URL |
|---|---|
| Producción | `https://api.reciclatru.example.com/api/v1` |
| Desarrollo local | `http://localhost:3000/api/v1` |

## Autenticación

Esquema `bearerAuth`: cabecera `Authorization: Bearer <accessToken>`. El token se obtiene en `POST /party-authentication/initiation`. Cada operación indica si es pública o requiere el token.

## Índice de endpoints

| Dominio BIAN | Método | Ruta | Acción BIAN | Resumen | operationId |
|---|---|---|---|---|---|
| Sistema | `GET` | `/health` | — | Estado del servicio | `getHealth` |
| Party Data Management | `POST` | `/party-data-management/initiation` | Initiate | Registrarme con un rol | `initiatePartyDataManagement` |
| Party Data Management | `GET` | `/party-data-management/{cr-reference-id}/retrieval` | Retrieve | Obtener mi perfil | `retrievePartyDataManagement` |
| Party Data Management | `PUT` | `/party-data-management/{cr-reference-id}/update` | Update | Completar o editar mi perfil | `updatePartyDataManagement` |
| Party Authentication | `POST` | `/party-authentication/initiation` | Initiate | Iniciar sesión | `initiatePartyAuthentication` |
| Location Data Management | `PUT` | `/location-data-management/{cr-reference-id}/pickup-point/update` | Update | Registrar mi ubicación de recojo (vecino) | `updateLocationDataManagementPickupPoint` |
| Servicing Order | `POST` | `/servicing-order/initiation` | Initiate | Publicar un pedido (vecino) | `initiateServicingOrder` |
| Servicing Order | `GET` | `/servicing-order/retrieval` | Retrieve | Mis pedidos (vecino) | `retrieveServicingOrderList` |
| Servicing Order | `GET` | `/servicing-order/availability/retrieval` | Retrieve | Pedidos disponibles (reciclador) | `retrieveServicingOrderAvailability` |
| Servicing Order | `GET` | `/servicing-order/acceptance/retrieval` | Retrieve | Mis pedidos aceptados (reciclador) | `retrieveServicingOrderAcceptanceList` |
| Servicing Order | `GET` | `/servicing-order/{cr-reference-id}/retrieval` | Retrieve | Detalle de un pedido | `retrieveServicingOrder` |
| Servicing Order | `PUT` | `/servicing-order/{cr-reference-id}/update` | Update | Editar un pedido (vecino) | `updateServicingOrder` |
| Servicing Order | `PUT` | `/servicing-order/{cr-reference-id}/termination` | Terminate | Cancelar un pedido (vecino) | `terminateServicingOrder` |
| Servicing Order | `POST` | `/servicing-order/{cr-reference-id}/photo/initiation` | Initiate | Subir una foto al pedido (vecino) | `initiateServicingOrderPhoto` |
| Servicing Order | `GET` | `/servicing-order/{cr-reference-id}/photo/{photo-id}/retrieval` | Retrieve | Obtener una foto del pedido | `retrieveServicingOrderPhoto` |
| Servicing Order | `PUT` | `/servicing-order/{cr-reference-id}/photo/{photo-id}/termination` | Terminate | Quitar una foto del pedido (vecino) | `terminateServicingOrderPhoto` |
| Servicing Order | `POST` | `/servicing-order/{cr-reference-id}/acceptance/initiation` | Initiate | Aceptar un pedido (reciclador) | `initiateServicingOrderAcceptance` |
| Servicing Order | `GET` | `/servicing-order/{cr-reference-id}/acceptance/retrieval` | Retrieve | Ver quiénes aceptaron mi pedido (vecino) | `retrieveServicingOrderAcceptance` |
| Servicing Order | `PUT` | `/servicing-order/{cr-reference-id}/confirmation/execution` | Execute | Elegir un reciclador (vecino) | `executeServicingOrderConfirmation` |
| Servicing Order | `PUT` | `/servicing-order/{cr-reference-id}/fulfillment/execution` | Execute | Marcar como Recolectado (vecino) | `executeServicingOrderFulfillment` |

## Códigos de error

Todos los errores usan `application/problem+json` (RFC 7807), esquema [Problem](#schema-problem). Cada error trae un `code` estable para la app.

| Código | HTTP | Operaciones |
|---|---|---|
| `VALIDATION_ERROR` | 400 | Respuesta común a varias operaciones |
| `UNAUTHENTICATED` | 401 | Respuesta común a varias operaciones |
| `FORBIDDEN_ROLE` | 403 | Respuesta común a varias operaciones |
| `NOT_CHOSEN_RECYCLER` | 403 | Respuesta común a varias operaciones |
| `NOT_ORDER_OWNER` | 403 | Respuesta común a varias operaciones |
| `NOT_FOUND` | 404 | Respuesta común a varias operaciones |
| `EMAIL_ALREADY_REGISTERED` | 409 | `initiatePartyDataManagement` |
| `ORDER_NOT_AVAILABLE` | 409 | `retrieveServicingOrder`, `initiateServicingOrderAcceptance` |
| `ORDER_NOT_CANCELLABLE` | 409 | `terminateServicingOrder` |
| `ORDER_NOT_CONFIRMED` | 409 | `executeServicingOrderFulfillment` |
| `ORDER_NOT_EDITABLE` | 409 | `updateServicingOrder`, `initiateServicingOrderPhoto`, `terminateServicingOrderPhoto` |
| `ORDER_NOT_PENDING` | 409 | `executeServicingOrderConfirmation` |
| `PHOTO_TOO_LARGE` | 413 | `initiateServicingOrderPhoto` |
| `INVALID_ITEMS` | 422 | `initiateServicingOrder`, `updateServicingOrder`, `initiateServicingOrderAcceptance` |
| `INVALID_WEIGHT` | 422 | `initiateServicingOrder`, `updateServicingOrder`, `initiateServicingOrderAcceptance` |
| `PHOTO_INVALID_TYPE` | 422 | `initiateServicingOrderPhoto` |
| `PHOTO_LIMIT_REACHED` | 422 | `initiateServicingOrderPhoto` |
| `PROFILE_INCOMPLETE` | 422 | `initiateServicingOrder`, `updateServicingOrder`, `initiateServicingOrderAcceptance` |

---

## Sistema

### GET `/health`

**Estado del servicio** · `operationId: getHealth`

Autenticación: pública

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Servicio activo | `application/json` |

Respuesta 200:

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `status` | string | sí |  |
| `version` | string | no |  |

```json
{
  "status": "UP",
  "version": "1.0.0"
}
```

---

## Party Data Management

### POST `/party-data-management/initiation`

**Registrarme con un rol** · `operationId: initiatePartyDataManagement`

BIAN: serviceDomain: **Party Data Management** · actionTerm: **Initiate**

HU-01. Crea la cuenta con correo, contraseña y rol. El correo se guarda en minúsculas y sin espacios,
con índice único. La contraseña se guarda con bcrypt. Tras registrarse, la app lleva a completar el perfil (HU-03).

Autenticación: pública

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [PartyDataManagementInitiateInputModel](#schema-partydatamanagementinitiateinputmodel)

Ejemplo `vecino`: Registro de un vecino

```json
{
  "email": "rosa.carranza@example.com",
  "password": "Reciclatru2026",
  "role": "NEIGHBOR"
}
```

Ejemplo `reciclador`: Registro de un reciclador

```json
{
  "email": "jorge.mendoza@example.com",
  "password": "Reciclatru2026",
  "role": "RECYCLER"
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 201 | Cuenta creada | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 409 | Ya existe una cuenta con ese correo (`EMAIL_ALREADY_REGISTERED`) | `application/problem+json` |

Respuesta 201:

Esquema: [PartyDataManagementOutputModel](#schema-partydatamanagementoutputmodel)

Ejemplo `vecino`: Cuenta recién creada: el perfil aún está incompleto (se completa en HU-03)

```json
{
  "crReferenceId": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
  "partyDataManagementInstanceRecord": {
    "id": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
    "email": "rosa.carranza@example.com",
    "role": "NEIGHBOR",
    "fullName": null,
    "phone": null,
    "reference": null,
    "pickupPoint": null,
    "profileComplete": false,
    "createdAt": "2026-10-20T13:42:10Z"
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 409 `duplicado`: Correo ya registrado

```json
{
  "type": "https://api.reciclatru.example.com/errors/email-already-registered",
  "title": "Correo ya registrado",
  "status": 409,
  "code": "EMAIL_ALREADY_REGISTERED",
  "detail": "Ya existe una cuenta con ese correo.",
  "instance": "/api/v1/party-data-management/initiation"
}
```

### GET `/party-data-management/{cr-reference-id}/retrieval`

**Obtener mi perfil** · `operationId: retrievePartyDataManagement`

BIAN: serviceDomain: **Party Data Management** · actionTerm: **Retrieve**

Usa `me` como `cr-reference-id` para el usuario autenticado. Incluye si el perfil está completo (RN20).

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Perfil del usuario autenticado | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |

Respuesta 200:

Esquema: [PartyDataManagementOutputModel](#schema-partydatamanagementoutputmodel)

Ejemplo `vecino_completo`: Vecino con perfil y ubicación completos (GET con `me`)

```json
{
  "crReferenceId": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
  "partyDataManagementInstanceRecord": {
    "id": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
    "email": "rosa.carranza@example.com",
    "role": "NEIGHBOR",
    "fullName": "Rosa Elena Carranza Vega",
    "phone": "987654321",
    "reference": "Casa de dos pisos con portón verde, frente al parque",
    "pickupPoint": {
      "latitude": -8.077806,
      "longitude": -79.122183,
      "address": "Av. Víctor Larco 1280, Huanchaco",
      "district": "Huanchaco"
    },
    "profileComplete": true,
    "createdAt": "2026-10-20T13:42:10Z"
  }
}
```

Ejemplo `reciclador`: Reciclador con perfil completo

```json
{
  "crReferenceId": "c4d5e6f7-0819-4a2b-9c3d-4e5f60718293",
  "partyDataManagementInstanceRecord": {
    "id": "c4d5e6f7-0819-4a2b-9c3d-4e5f60718293",
    "email": "jorge.mendoza@example.com",
    "role": "RECYCLER",
    "fullName": "Jorge Luis Mendoza Ruiz",
    "phone": "944123456",
    "reference": null,
    "pickupPoint": null,
    "profileComplete": true,
    "createdAt": "2026-10-19T16:05:40Z"
  }
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

### PUT `/party-data-management/{cr-reference-id}/update`

**Completar o editar mi perfil** · `operationId: updatePartyDataManagement`

BIAN: serviceDomain: **Party Data Management** · actionTerm: **Update**

HU-03. Nombre y apellido y celular son obligatorios (se validan también en el backend).
El vecino puede agregar una referencia opcional de hasta 150 caracteres.
El correo y el rol no son editables.
Si cambia el celular, las contrapartes con pedidos activos ven el número nuevo.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [PartyDataManagementUpdateInputModel](#schema-partydatamanagementupdateinputmodel)

Ejemplo `vecino`: Vecino completa su perfil con referencia

```json
{
  "fullName": "Rosa Elena Carranza Vega",
  "phone": "987654321",
  "reference": "Casa de dos pisos con portón verde, frente al parque"
}
```

Ejemplo `reciclador`: Reciclador completa su perfil

```json
{
  "fullName": "Jorge Luis Mendoza Ruiz",
  "phone": "944123456"
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Perfil actualizado | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |

Respuesta 200:

Esquema: [PartyDataManagementOutputModel](#schema-partydatamanagementoutputmodel)

Ejemplo `vecino_sin_ubicacion`: Perfil guardado; falta marcar la ubicación (profileComplete = false)

```json
{
  "crReferenceId": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
  "partyDataManagementInstanceRecord": {
    "id": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
    "email": "rosa.carranza@example.com",
    "role": "NEIGHBOR",
    "fullName": "Rosa Elena Carranza Vega",
    "phone": "987654321",
    "reference": "Casa de dos pisos con portón verde, frente al parque",
    "pickupPoint": null,
    "profileComplete": false,
    "createdAt": "2026-10-20T13:42:10Z"
  }
}
```

Ejemplo `reciclador`: Reciclador con perfil completo

```json
{
  "crReferenceId": "c4d5e6f7-0819-4a2b-9c3d-4e5f60718293",
  "partyDataManagementInstanceRecord": {
    "id": "c4d5e6f7-0819-4a2b-9c3d-4e5f60718293",
    "email": "jorge.mendoza@example.com",
    "role": "RECYCLER",
    "fullName": "Jorge Luis Mendoza Ruiz",
    "phone": "944123456",
    "reference": null,
    "pickupPoint": null,
    "profileComplete": true,
    "createdAt": "2026-10-19T16:05:40Z"
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

---

## Party Authentication

### POST `/party-authentication/initiation`

**Iniciar sesión** · `operationId: initiatePartyAuthentication`

BIAN: serviceDomain: **Party Authentication** · actionTerm: **Initiate**

HU-02. Devuelve un JWT que vence a los 30 días. Si los datos son incorrectos, el mensaje no indica cuál falló.
La app guarda el token en DataStore y lo envía en cada petición. Cerrar sesión solo borra el token en el teléfono.
Si el backend responde 401 en cualquier endpoint, la app borra el token y vuelve al inicio de sesión.

Autenticación: pública

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [PartyAuthenticationInitiateInputModel](#schema-partyauthenticationinitiateinputmodel)

Ejemplo `login`: Inicio de sesión

```json
{
  "email": "rosa.carranza@example.com",
  "password": "Reciclatru2026"
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Sesión iniciada | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |

Respuesta 200:

Esquema: [PartyAuthenticationOutputModel](#schema-partyauthenticationoutputmodel)

Ejemplo `vecino`: JWT con vigencia de 30 días

```json
{
  "crReferenceId": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI3YjFjMmQzZS00ZjUwLTRhNjEtOGI3Mi05M2E0YjVjNmQ3ZTgiLCJyb2xlIjoiTkVJR0hCT1IiLCJpYXQiOjE3OTMwMjY4MDAsImV4cCI6MTc5NTYxODgwMH0.k3J9xV2mQ7pLw0YtB8nZc1RfD5uHsEaGoKiNj4TbXyU",
  "tokenType": "Bearer",
  "expiresAt": "2026-11-25T15:00:00Z",
  "partyDataManagementInstanceRecord": {
    "id": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
    "email": "rosa.carranza@example.com",
    "role": "NEIGHBOR",
    "fullName": "Rosa Elena Carranza Vega",
    "phone": "987654321",
    "reference": "Casa de dos pisos con portón verde, frente al parque",
    "pickupPoint": {
      "latitude": -8.077806,
      "longitude": -79.122183,
      "address": "Av. Víctor Larco 1280, Huanchaco",
      "district": "Huanchaco"
    },
    "profileComplete": true,
    "createdAt": "2026-10-20T13:42:10Z"
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

---

## Location Data Management

### PUT `/location-data-management/{cr-reference-id}/pickup-point/update`

**Registrar mi ubicación de recojo (vecino)** · `operationId: updateLocationDataManagementPickupPoint`

BIAN: serviceDomain: **Location Data Management** · behaviorQualifier: **PickupPoint** · actionTerm: **Update**

HU-04. Guarda el punto marcado en el mapa (centro del mapa, 6 decimales), la dirección y el distrito.
No usa GPS. Cada pedido copia esta ubicación al publicarse; si se cambia, solo afecta a los pedidos posteriores (RN18).
Usa `me` como `cr-reference-id`.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [PickupPointUpdateInputModel](#schema-pickuppointupdateinputmodel)

Ejemplo `huanchaco`: Punto marcado en el mapa (6 decimales)

```json
{
  "latitude": -8.077806,
  "longitude": -79.122183,
  "address": "Av. Víctor Larco 1280, Huanchaco",
  "district": "Huanchaco"
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Ubicación guardada | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |

Respuesta 200:

Esquema: [PartyDataManagementOutputModel](#schema-partydatamanagementoutputmodel)

Ejemplo `vecino_completo`: Con la ubicación el perfil queda completo

```json
{
  "crReferenceId": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
  "partyDataManagementInstanceRecord": {
    "id": "7b1c2d3e-4f50-4a61-8b72-93a4b5c6d7e8",
    "email": "rosa.carranza@example.com",
    "role": "NEIGHBOR",
    "fullName": "Rosa Elena Carranza Vega",
    "phone": "987654321",
    "reference": "Casa de dos pisos con portón verde, frente al parque",
    "pickupPoint": {
      "latitude": -8.077806,
      "longitude": -79.122183,
      "address": "Av. Víctor Larco 1280, Huanchaco",
      "district": "Huanchaco"
    },
    "profileComplete": true,
    "createdAt": "2026-10-20T13:42:10Z"
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

---

## Servicing Order

### POST `/servicing-order/initiation`

**Publicar un pedido (vecino)** · `operationId: initiateServicingOrder`

BIAN: serviceDomain: **Servicing Order** · actionTerm: **Initiate**

HU-05. De 1 a 10 ítems. Requiere perfil completo, incluida la ubicación (RN20).
El pedido queda `PENDING` con la ubicación del perfil. Las fotos se suben aparte (HU-09).
Se puede tener varios pedidos a la vez.

Autenticación: Bearer JWT

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [ServicingOrderInitiateInputModel](#schema-servicingorderinitiateinputmodel)

Ejemplo `dos_items`: Pedido con dos ítems

```json
{
  "items": [
    {
      "description": "Botellas de plástico PET",
      "weightKg": 4.5
    },
    {
      "description": "Cartón corrugado",
      "weightKg": 8.0
    }
  ]
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 201 | Pedido creado | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 422 | Regla de negocio incumplida (`PROFILE_INCOMPLETE`, `INVALID_ITEMS`, `INVALID_WEIGHT`, etc.) | `application/problem+json` |

Respuesta 201:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `recien_publicado`: PENDING sin fotos ni aceptaciones (las fotos se suben aparte)

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      }
    ],
    "totalWeightKg": 12.5,
    "photos": [],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 0,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [
      "EDIT",
      "CANCEL"
    ],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 422 `perfil`: Perfil incompleto

```json
{
  "type": "https://api.reciclatru.example.com/errors/profile-incomplete",
  "title": "Perfil incompleto",
  "status": 422,
  "code": "PROFILE_INCOMPLETE",
  "detail": "Completa tu nombre, celular y ubicación antes de publicar un pedido.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `items`: Más de 10 o ningún ítem

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-items",
  "title": "Cantidad de ítems inválida",
  "status": 422,
  "code": "INVALID_ITEMS",
  "detail": "Un pedido debe tener entre 1 y 10 ítems.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `peso`: Peso fuera de rango

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-weight",
  "title": "Peso inválido",
  "status": 422,
  "code": "INVALID_WEIGHT",
  "detail": "El peso de cada ítem debe ser mayor que 0 y como máximo 500 kg.",
  "instance": "/api/v1/servicing-order/initiation",
  "errors": [
    {
      "field": "items[1].weightKg",
      "message": "Debe ser mayor que 0 y como máximo 500"
    }
  ]
}
```

### GET `/servicing-order/retrieval`

**Mis pedidos (vecino)** · `operationId: retrieveServicingOrderList`

BIAN: serviceDomain: **Servicing Order** · actionTerm: **Retrieve**

HU-06. Solo devuelve los pedidos del vecino del token. Resumen liviano, de 20 en 20, del más reciente al más antiguo.
`tab=ACTIVE` agrupa `PENDING` y `CONFIRMED`; `tab=HISTORY` agrupa `COLLECTED` y `CANCELLED`.
Si está `PENDING`, incluye `acceptanceCount`; si está `CONFIRMED`, el nombre del reciclador elegido.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `tab` | query | no | enum: `ACTIVE`, `HISTORY` |  |
| `limit` | query | no | integer · mín 1, máx 50 |  |
| `cursor` | query | no | string | Valor `nextCursor` de la página anterior |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Página de pedidos | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderListOutputModel](#schema-servicingorderlistoutputmodel)

Ejemplo `activos`: Pestaña ACTIVE: pendiente y confirmado

```json
{
  "items": [
    {
      "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 13.7,
      "thumbnailUrl": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": 2,
      "chosenRecyclerName": null,
      "alreadyAccepted": null,
      "createdAt": "2026-10-26T14:05:00Z"
    },
    {
      "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
      "status": "CONFIRMED",
      "itemCount": 2,
      "totalWeightKg": 11.5,
      "thumbnailUrl": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
      "alreadyAccepted": null,
      "createdAt": "2026-10-23T14:10:00Z"
    }
  ],
  "nextCursor": null
}
```

Ejemplo `activos_con_pagina`: Hay más páginas

```json
{
  "items": [
    {
      "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 13.7,
      "thumbnailUrl": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": 2,
      "chosenRecyclerName": null,
      "alreadyAccepted": null,
      "createdAt": "2026-10-26T14:05:00Z"
    },
    {
      "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
      "status": "CONFIRMED",
      "itemCount": 2,
      "totalWeightKg": 11.5,
      "thumbnailUrl": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
      "alreadyAccepted": null,
      "createdAt": "2026-10-23T14:10:00Z"
    }
  ],
  "nextCursor": "eyJjIjoiMjAyNi0xMC0yM1QxNDoxMDowMFoiLCJpIjoiNWIyZTlkNDEtOGMzNy00ZjA2LWExZDgtN2UzYzZiOWYwYTI0In0"
}
```

Ejemplo `historial`: Pestaña HISTORY: recolectado y cancelado

```json
{
  "items": [
    {
      "id": "e17c4a90-3b58-4d2f-8a6e-1f9d0c7b5e83",
      "status": "COLLECTED",
      "itemCount": 2,
      "totalWeightKg": 8.0,
      "thumbnailUrl": "/api/v1/servicing-order/e17c4a90-3b58-4d2f-8a6e-1f9d0c7b5e83/photo/e8f90a1b-2b34-4c45-a6e7-08192a3b4c53/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": null,
      "createdAt": "2026-10-18T13:00:00Z"
    },
    {
      "id": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
      "status": "CANCELLED",
      "itemCount": 1,
      "totalWeightKg": 3.0,
      "thumbnailUrl": "/api/v1/servicing-order/92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70/photo/c5d6e7f8-0912-4b23-84c5-e6f7a8091a31/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": 1,
      "chosenRecyclerName": null,
      "alreadyAccepted": null,
      "createdAt": "2026-10-25T19:00:00Z"
    }
  ],
  "nextCursor": null
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

### GET `/servicing-order/availability/retrieval`

**Pedidos disponibles (reciclador)** · `operationId: retrieveServicingOrderAvailability`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Availability** · actionTerm: **Retrieve**

HU-10. Solo pedidos `PENDING`. Si se envían `lat` y `lng` (ACCESS_COARSE_LOCATION), se ordenan del más cercano al más lejano
con la fórmula de Haversine en PostgreSQL; sin ubicación, se ordenan por fecha. **La ubicación no se guarda.**
Nunca incluye dirección, nombre ni teléfono del vecino. La distancia se devuelve aproximada (redondeada a 0,1 km).
Los pedidos ya aceptados por el reciclador llevan `alreadyAccepted = true`. De 20 en 20.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `lat` | query | no | number (double) · mín -90, máx 90 |  |
| `lng` | query | no | number (double) · mín -180, máx 180 |  |
| `limit` | query | no | integer · mín 1, máx 50 |  |
| `cursor` | query | no | string | Valor `nextCursor` de la página anterior |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Pedidos disponibles | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderAvailabilityOutputModel](#schema-servicingorderavailabilityoutputmodel)

Ejemplo `con_ubicacion`: Con lat y lng: ordenado del más cercano al más lejano

```json
{
  "sortedBy": "DISTANCE",
  "items": [
    {
      "id": "b8e2c6a4-1d73-4f95-a0b6-3e9d7c2f5a18",
      "status": "PENDING",
      "itemCount": 2,
      "totalWeightKg": 6.4,
      "thumbnailUrl": null,
      "zone": "Víctor Larco Herrera",
      "approximateDistanceKm": 0.8,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": false,
      "createdAt": "2026-10-26T13:20:00Z"
    },
    {
      "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 13.7,
      "thumbnailUrl": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": 3.4,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": true,
      "createdAt": "2026-10-26T14:05:00Z"
    },
    {
      "id": "4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 21.3,
      "thumbnailUrl": "/api/v1/servicing-order/4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05/photo/f90a1b2c-3c45-4d56-b7f8-192a3b4c5d64/retrieval",
      "zone": "Trujillo",
      "approximateDistanceKm": 6.1,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": false,
      "createdAt": "2026-10-25T21:45:00Z"
    }
  ],
  "nextCursor": null
}
```

Ejemplo `sin_ubicacion`: Sin ubicación: ordenado por fecha, sin distancia

```json
{
  "sortedBy": "DATE",
  "items": [
    {
      "id": "b8e2c6a4-1d73-4f95-a0b6-3e9d7c2f5a18",
      "status": "PENDING",
      "itemCount": 2,
      "totalWeightKg": 6.4,
      "thumbnailUrl": null,
      "zone": "Víctor Larco Herrera",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": false,
      "createdAt": "2026-10-26T13:20:00Z"
    },
    {
      "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 13.7,
      "thumbnailUrl": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval",
      "zone": "Huanchaco",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": true,
      "createdAt": "2026-10-26T14:05:00Z"
    },
    {
      "id": "4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05",
      "status": "PENDING",
      "itemCount": 3,
      "totalWeightKg": 21.3,
      "thumbnailUrl": "/api/v1/servicing-order/4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05/photo/f90a1b2c-3c45-4d56-b7f8-192a3b4c5d64/retrieval",
      "zone": "Trujillo",
      "approximateDistanceKm": null,
      "acceptanceCount": null,
      "chosenRecyclerName": null,
      "alreadyAccepted": false,
      "createdAt": "2026-10-25T21:45:00Z"
    }
  ],
  "nextCursor": null
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

### GET `/servicing-order/acceptance/retrieval`

**Mis pedidos aceptados (reciclador)** · `operationId: retrieveServicingOrderAcceptanceList`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Acceptance** · actionTerm: **Retrieve**

HU-14. Solo las aceptaciones del reciclador del token, de 20 en 20. Cada una trae `acceptanceLabel`, que combina el
estado de la aceptación con el del pedido. `tab=ACTIVE` agrupa `AWAITING_CONFIRMATION` y `CHOSEN`
(los elegidos primero); `tab=HISTORY` agrupa `NOT_CHOSEN`, `CANCELLED_BY_NEIGHBOR` y `COLLECTED`.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `tab` | query | no | enum: `ACTIVE`, `HISTORY` |  |
| `limit` | query | no | integer · mín 1, máx 50 |  |
| `cursor` | query | no | string | Valor `nextCursor` de la página anterior |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Aceptaciones del reciclador | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |

Respuesta 200:

Esquema: [MyAcceptanceListOutputModel](#schema-myacceptancelistoutputmodel)

Ejemplo `activas`: Pestaña ACTIVE: los elegidos primero

```json
{
  "items": [
    {
      "orderId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
      "acceptanceLabel": "CHOSEN",
      "zone": "Huanchaco",
      "totalWeightKg": 11.5,
      "acceptedAt": "2026-10-23T18:40:00Z"
    },
    {
      "orderId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
      "acceptanceLabel": "AWAITING_CONFIRMATION",
      "zone": "Huanchaco",
      "totalWeightKg": 13.7,
      "acceptedAt": "2026-10-26T15:10:00Z"
    }
  ],
  "nextCursor": null
}
```

Ejemplo `historial`: Pestaña HISTORY

```json
{
  "items": [
    {
      "orderId": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
      "acceptanceLabel": "CANCELLED_BY_NEIGHBOR",
      "zone": "Huanchaco",
      "totalWeightKg": 3.0,
      "acceptedAt": "2026-10-25T19:30:00Z"
    },
    {
      "orderId": "e17c4a90-3b58-4d2f-8a6e-1f9d0c7b5e83",
      "acceptanceLabel": "COLLECTED",
      "zone": "Huanchaco",
      "totalWeightKg": 8.0,
      "acceptedAt": "2026-10-18T15:00:00Z"
    },
    {
      "orderId": "1c5e8a37-9b04-4d62-a7f3-d6b2e0c49a81",
      "acceptanceLabel": "NOT_CHOSEN",
      "zone": "La Esperanza",
      "totalWeightKg": 7.2,
      "acceptedAt": "2026-10-20T15:00:00Z"
    }
  ],
  "nextCursor": null
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

### GET `/servicing-order/{cr-reference-id}/retrieval`

**Detalle de un pedido** · `operationId: retrieveServicingOrder`

BIAN: serviceDomain: **Servicing Order** · actionTerm: **Retrieve**

HU-06, HU-11, HU-15. El backend decide qué campos enviar según el rol y el estado (no basta con ocultarlos en la app):
- **Vecino dueño:** ítems, fotos, estado y acciones posibles.
- **Reciclador sin aceptar:** ítems, pesos, fotos, zona, distancia aproximada y cantidad de aceptaciones. Sin dirección, referencia, nombre ni teléfono.
- **Reciclador con aceptación `ACCEPTED` o `CHOSEN`, pedido `PENDING` o `CONFIRMED`:** además `counterpart` (nombre y teléfono del vecino).
- **Reciclador elegido con el pedido `CONFIRMED`:** además dirección, referencia y coordenadas para «Cómo llegar».
- **Pedido ya no `PENDING` y reciclador sin aceptación:** 409 `ORDER_NOT_AVAILABLE` (la app muestra «Este pedido ya no está disponible»).
Un reciclador que ya fue descartado, o con el pedido cancelado, solo ve la información del pedido, sin dirección ni teléfono.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |
| `lat` | query | no | number (double) | Opcional. Solo para calcular la distancia aproximada al reciclador; no se guarda. |
| `lng` | query | no | number (double) |  |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Pedido | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido ya no está disponible para este reciclador (`ORDER_NOT_AVAILABLE`) | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `vecino_pendiente`: Vecino dueño, PENDING con 2 aceptaciones

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      },
      {
        "description": "Latas de aluminio",
        "weightKg": 1.2
      }
    ],
    "totalWeightKg": 13.7,
    "photos": [
      {
        "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
      },
      {
        "id": "b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 2,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [
      "CHOOSE_RECYCLER",
      "CANCEL"
    ],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Ejemplo `vecino_confirmado`: Vecino dueño, CONFIRMED: ve el contacto del reciclador elegido

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "CONFIRMED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 2,
    "acceptanceStatus": null,
    "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
    "counterpart": {
      "fullName": "Jorge Luis Mendoza Ruiz",
      "phone": "944123456"
    },
    "pickup": null,
    "availableActions": [
      "MARK_COLLECTED",
      "CANCEL"
    ],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Ejemplo `vecino_recolectado`: Vecino dueño, COLLECTED (estado final)

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "COLLECTED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 2,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": "2026-10-26T20:15:00Z",
    "cancelledAt": null
  }
}
```

Ejemplo `vecino_cancelado`: Vecino dueño, CANCELLED (estado final)

```json
{
  "crReferenceId": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
  "servicingOrderInstanceRecord": {
    "id": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
    "status": "CANCELLED",
    "items": [
      {
        "description": "Cajas de cartón",
        "weightKg": 3.0
      }
    ],
    "totalWeightKg": 3.0,
    "photos": [
      {
        "id": "c5d6e7f8-0912-4b23-84c5-e6f7a8091a31",
        "url": "/api/v1/servicing-order/92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70/photo/c5d6e7f8-0912-4b23-84c5-e6f7a8091a31/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 1,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-25T19:00:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": "2026-10-25T20:00:00Z"
  }
}
```

Ejemplo `reciclador_sin_aceptar`: Reciclador que aún no acepta: sin dirección, nombre ni teléfono

```json
{
  "crReferenceId": "b8e2c6a4-1d73-4f95-a0b6-3e9d7c2f5a18",
  "servicingOrderInstanceRecord": {
    "id": "b8e2c6a4-1d73-4f95-a0b6-3e9d7c2f5a18",
    "status": "PENDING",
    "items": [
      {
        "description": "Latas de aluminio",
        "weightKg": 2.4
      },
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.0
      }
    ],
    "totalWeightKg": 6.4,
    "photos": [],
    "zone": "Víctor Larco Herrera",
    "approximateDistanceKm": 0.8,
    "acceptanceCount": 0,
    "alreadyAccepted": false,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [
      "ACCEPT"
    ],
    "createdAt": "2026-10-26T13:20:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Ejemplo `reciclador_aceptado`: Reciclador con aceptación ACCEPTED: ve nombre y teléfono, no la dirección

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      },
      {
        "description": "Latas de aluminio",
        "weightKg": 1.2
      }
    ],
    "totalWeightKg": 13.7,
    "photos": [
      {
        "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
      },
      {
        "id": "b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": 3.4,
    "acceptanceCount": 2,
    "alreadyAccepted": true,
    "acceptanceStatus": "ACCEPTED",
    "chosenRecyclerName": null,
    "counterpart": {
      "fullName": "Rosa Elena Carranza Vega",
      "phone": "987654321"
    },
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Ejemplo `reciclador_elegido`: Reciclador elegido con el pedido CONFIRMED: ve la dirección exacta (RN13)

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "CONFIRMED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": 3.4,
    "acceptanceCount": 2,
    "alreadyAccepted": true,
    "acceptanceStatus": "CHOSEN",
    "chosenRecyclerName": null,
    "counterpart": {
      "fullName": "Rosa Elena Carranza Vega",
      "phone": "987654321"
    },
    "pickup": {
      "address": "Av. Víctor Larco 1280, Huanchaco",
      "reference": "Casa de dos pisos con portón verde, frente al parque",
      "latitude": -8.077806,
      "longitude": -79.122183
    },
    "availableActions": [
      "GO_PICKUP"
    ],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Ejemplo `reciclador_descartado`: Reciclador DISCARDED: solo información del pedido, sin dirección ni teléfono

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "CONFIRMED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": 3.4,
    "acceptanceCount": 2,
    "alreadyAccepted": true,
    "acceptanceStatus": "DISCARDED",
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_disponible`: Reciclador sin aceptación y pedido ya no PENDING

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-available",
  "title": "Este pedido ya no está disponible",
  "status": 409,
  "code": "ORDER_NOT_AVAILABLE",
  "detail": "El pedido ya no está pendiente.",
  "instance": "/api/v1/servicing-order/4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05/acceptance/initiation"
}
```

### PUT `/servicing-order/{cr-reference-id}/update`

**Editar un pedido (vecino)** · `operationId: updateServicingOrder`

BIAN: serviceDomain: **Servicing Order** · actionTerm: **Update**

HU-07 (RN11). Solo si el pedido está `PENDING` y sin aceptaciones; si no, 409 `ORDER_NOT_EDITABLE`.
Aplica las mismas validaciones que al publicar. La ubicación no se edita.
Las fotos se agregan o quitan con los endpoints de fotos.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Cuerpo de la solicitud** (obligatorio)

`application/json`

Esquema: [ServicingOrderUpdateInputModel](#schema-servicingorderupdateinputmodel)

Ejemplo `agregar_item`: Se agrega un ítem al pedido

```json
{
  "items": [
    {
      "description": "Botellas de plástico PET",
      "weightKg": 4.5
    },
    {
      "description": "Cartón corrugado",
      "weightKg": 8.0
    },
    {
      "description": "Latas de aluminio",
      "weightKg": 1.2
    }
  ]
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Pedido actualizado | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | Ya tiene aceptaciones o no está `PENDING` (`ORDER_NOT_EDITABLE`) | `application/problem+json` |
| 422 | Regla de negocio incumplida (`PROFILE_INCOMPLETE`, `INVALID_ITEMS`, `INVALID_WEIGHT`, etc.) | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `editado`: Pedido PENDING editado

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      },
      {
        "description": "Latas de aluminio",
        "weightKg": 1.2
      }
    ],
    "totalWeightKg": 13.7,
    "photos": [
      {
        "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
      },
      {
        "id": "b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 0,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [
      "EDIT",
      "CANCEL"
    ],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_editable`: Ya tiene aceptaciones

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-editable",
  "title": "El pedido ya no se puede editar",
  "status": 409,
  "code": "ORDER_NOT_EDITABLE",
  "detail": "El pedido ya tiene aceptaciones o dejó de estar pendiente.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 422 `perfil`: Perfil incompleto

```json
{
  "type": "https://api.reciclatru.example.com/errors/profile-incomplete",
  "title": "Perfil incompleto",
  "status": 422,
  "code": "PROFILE_INCOMPLETE",
  "detail": "Completa tu nombre, celular y ubicación antes de publicar un pedido.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `items`: Más de 10 o ningún ítem

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-items",
  "title": "Cantidad de ítems inválida",
  "status": 422,
  "code": "INVALID_ITEMS",
  "detail": "Un pedido debe tener entre 1 y 10 ítems.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `peso`: Peso fuera de rango

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-weight",
  "title": "Peso inválido",
  "status": 422,
  "code": "INVALID_WEIGHT",
  "detail": "El peso de cada ítem debe ser mayor que 0 y como máximo 500 kg.",
  "instance": "/api/v1/servicing-order/initiation",
  "errors": [
    {
      "field": "items[1].weightKg",
      "message": "Debe ser mayor que 0 y como máximo 500"
    }
  ]
}
```

### PUT `/servicing-order/{cr-reference-id}/termination`

**Cancelar un pedido (vecino)** · `operationId: terminateServicingOrder`

BIAN: serviceDomain: **Servicing Order** · actionTerm: **Terminate**

HU-08 (RN16, RN17). Solo si está `PENDING` o `CONFIRMED`; si no, 409 `ORDER_NOT_CANCELLABLE`.
Es un borrado lógico: el pedido y sus fotos quedan en el historial. No se puede reactivar.
Los recicladores que lo aceptaron lo ven como `CANCELLED_BY_NEIGHBOR`; sus aceptaciones no cambian de estado
y dejan de ver la dirección y el teléfono.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Pedido cancelado | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | Ya recolectado o cancelado (`ORDER_NOT_CANCELLABLE`) | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `cancelado`: Pedido cancelado (borrado lógico)

```json
{
  "crReferenceId": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
  "servicingOrderInstanceRecord": {
    "id": "92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70",
    "status": "CANCELLED",
    "items": [
      {
        "description": "Cajas de cartón",
        "weightKg": 3.0
      }
    ],
    "totalWeightKg": 3.0,
    "photos": [
      {
        "id": "c5d6e7f8-0912-4b23-84c5-e6f7a8091a31",
        "url": "/api/v1/servicing-order/92d6b0f3-a481-4c7e-b5d9-6a3e8f1c2b70/photo/c5d6e7f8-0912-4b23-84c5-e6f7a8091a31/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 1,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-25T19:00:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": "2026-10-25T20:00:00Z"
  }
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_cancelable`: Ya recolectado o cancelado

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-cancellable",
  "title": "El pedido no se puede cancelar",
  "status": 409,
  "code": "ORDER_NOT_CANCELLABLE",
  "detail": "El pedido ya fue recolectado o cancelado.",
  "instance": "/api/v1/servicing-order/e17c4a90-3b58-4d2f-8a6e-1f9d0c7b5e83/termination"
}
```

### POST `/servicing-order/{cr-reference-id}/photo/initiation`

**Subir una foto al pedido (vecino)** · `operationId: initiateServicingOrderPhoto`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Photo** · actionTerm: **Initiate**

HU-09 (RN07). Se llama después de crear el pedido, una vez por foto. `multipart/form-data`.
El backend valida el máximo de 3 por pedido, el tipo real (JPEG o PNG) y el tamaño (máx. 2 MB), sube la imagen
a un bucket privado con clave única `{order_id}/{uuid}.jpg` y guarda solo esa clave en la base de datos.
La app comprime cada foto a unos 300 KB antes de enviarla. Solo mientras el pedido sea editable (HU-07).

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Cuerpo de la solicitud** (obligatorio)

`multipart/form-data`

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `file` | string (binary) | sí | Imagen JPEG o PNG (tipos y tamaño máximo a confirmar por el equipo). |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 201 | Foto guardada | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido ya no es editable (`ORDER_NOT_EDITABLE`) | `application/problem+json` |
| 413 | Archivo demasiado grande (`PHOTO_TOO_LARGE`) | `application/problem+json` |
| 422 | Ya tiene 3 fotos o el tipo no es válido (`PHOTO_LIMIT_REACHED`, `PHOTO_INVALID_TYPE`) | `application/problem+json` |

Respuesta 201:

Esquema: [OrderPhoto](#schema-orderphoto)

Ejemplo `foto`: Foto guardada; `url` es la ruta estable del endpoint de recuperación

```json
{
  "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
  "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_editable`: El pedido ya no es editable

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-editable",
  "title": "El pedido ya no se puede editar",
  "status": 409,
  "code": "ORDER_NOT_EDITABLE",
  "detail": "El pedido ya tiene aceptaciones o dejó de estar pendiente.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 413 `muy_grande`: Foto de más de 2 MB

```json
{
  "type": "https://api.reciclatru.example.com/errors/photo-too-large",
  "title": "La foto es demasiado grande",
  "status": 413,
  "code": "PHOTO_TOO_LARGE",
  "detail": "El tamaño máximo por foto es 2 MB.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/initiation"
}
```

Error 422 `limite`: Ya tiene 3 fotos

```json
{
  "type": "https://api.reciclatru.example.com/errors/photo-limit-reached",
  "title": "Límite de fotos alcanzado",
  "status": 422,
  "code": "PHOTO_LIMIT_REACHED",
  "detail": "Un pedido admite hasta 3 fotos.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/initiation"
}
```

Error 422 `tipo`: Archivo que no es JPEG ni PNG

```json
{
  "type": "https://api.reciclatru.example.com/errors/photo-invalid-type",
  "title": "Tipo de archivo no válido",
  "status": 422,
  "code": "PHOTO_INVALID_TYPE",
  "detail": "Solo se admiten imágenes JPEG o PNG.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/initiation"
}
```

### GET `/servicing-order/{cr-reference-id}/photo/{photo-id}/retrieval`

**Obtener una foto del pedido** · `operationId: retrieveServicingOrderPhoto`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Photo** · actionTerm: **Retrieve**

Verifica que el usuario pueda ver el pedido (vecino dueño o reciclador que puede verlo) y responde 302 hacia una URL firmada de vida corta (5 minutos) del almacenamiento privado. La app sigue la redirección con Coil. No se entrega nunca una URL permanente.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |
| `photo-id` | path | sí | string (uuid) |  |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 302 | Redirección a la URL firmada de la imagen | Location |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

### PUT `/servicing-order/{cr-reference-id}/photo/{photo-id}/termination`

**Quitar una foto del pedido (vecino)** · `operationId: terminateServicingOrderPhoto`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Photo** · actionTerm: **Terminate**

Solo mientras el pedido sea editable. La foto se elimina también del bucket de almacenamiento.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |
| `photo-id` | path | sí | string (uuid) |  |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 204 | Foto eliminada | — |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido ya no es editable (`ORDER_NOT_EDITABLE`) | `application/problem+json` |

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_editable`: El pedido ya no es editable

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-editable",
  "title": "El pedido ya no se puede editar",
  "status": 409,
  "code": "ORDER_NOT_EDITABLE",
  "detail": "El pedido ya tiene aceptaciones o dejó de estar pendiente.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

### POST `/servicing-order/{cr-reference-id}/acceptance/initiation`

**Aceptar un pedido (reciclador)** · `operationId: initiateServicingOrderAcceptance`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Acceptance** · actionTerm: **Initiate**

HU-12 (RN09). Crea una aceptación en estado `ACCEPTED`. Varios recicladores pueden aceptar el mismo pedido mientras esté `PENDING`.
Un reciclador acepta cada pedido una sola vez (restricción única en base de datos; un doble toque registra una sola aceptación).
Requiere perfil completo. Revela nombre y teléfono del vecino, no la dirección.
Se ejecuta en una transacción; si el pedido ya no está `PENDING`, 409 `ORDER_NOT_AVAILABLE`.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 201 | Aceptación registrada | `application/json` |
| 200 | Ya había aceptado este pedido (doble toque); devuelve el pedido sin duplicar | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido ya no está `PENDING` (`ORDER_NOT_AVAILABLE`) | `application/problem+json` |
| 422 | Regla de negocio incumplida (`PROFILE_INCOMPLETE`, `INVALID_ITEMS`, `INVALID_WEIGHT`, etc.) | `application/problem+json` |

Respuesta 201:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `aceptado`: Aceptación creada (ACCEPTED): revela nombre y teléfono del vecino

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      },
      {
        "description": "Latas de aluminio",
        "weightKg": 1.2
      }
    ],
    "totalWeightKg": 13.7,
    "photos": [
      {
        "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
      },
      {
        "id": "b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 1,
    "alreadyAccepted": true,
    "acceptanceStatus": "ACCEPTED",
    "chosenRecyclerName": null,
    "counterpart": {
      "fullName": "Rosa Elena Carranza Vega",
      "phone": "987654321"
    },
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `ya_aceptado`: Doble toque: ya existía la aceptación

```json
{
  "crReferenceId": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
  "servicingOrderInstanceRecord": {
    "id": "0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36",
    "status": "PENDING",
    "items": [
      {
        "description": "Botellas de plástico PET",
        "weightKg": 4.5
      },
      {
        "description": "Cartón corrugado",
        "weightKg": 8.0
      },
      {
        "description": "Latas de aluminio",
        "weightKg": 1.2
      }
    ],
    "totalWeightKg": 13.7,
    "photos": [
      {
        "id": "a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/a3b4c5d6-e7f8-4091-a2b3-c4d5e6f70819/retrieval"
      },
      {
        "id": "b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920",
        "url": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/photo/b4c5d6e7-f809-4a12-b3c4-d5e6f7a80920/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 1,
    "alreadyAccepted": true,
    "acceptanceStatus": "ACCEPTED",
    "chosenRecyclerName": null,
    "counterpart": {
      "fullName": "Rosa Elena Carranza Vega",
      "phone": "987654321"
    },
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-26T14:05:00Z",
    "confirmedAt": null,
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_disponible`: El pedido ya no está PENDING

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-available",
  "title": "Este pedido ya no está disponible",
  "status": 409,
  "code": "ORDER_NOT_AVAILABLE",
  "detail": "El pedido ya no está pendiente.",
  "instance": "/api/v1/servicing-order/4a7f0d93-e5c1-4b28-9d6a-8f2b1e7c3a05/acceptance/initiation"
}
```

Error 422 `perfil`: Perfil incompleto

```json
{
  "type": "https://api.reciclatru.example.com/errors/profile-incomplete",
  "title": "Perfil incompleto",
  "status": 422,
  "code": "PROFILE_INCOMPLETE",
  "detail": "Completa tu nombre, celular y ubicación antes de publicar un pedido.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `items`: Más de 10 o ningún ítem

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-items",
  "title": "Cantidad de ítems inválida",
  "status": 422,
  "code": "INVALID_ITEMS",
  "detail": "Un pedido debe tener entre 1 y 10 ítems.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 422 `peso`: Peso fuera de rango

```json
{
  "type": "https://api.reciclatru.example.com/errors/invalid-weight",
  "title": "Peso inválido",
  "status": 422,
  "code": "INVALID_WEIGHT",
  "detail": "El peso de cada ítem debe ser mayor que 0 y como máximo 500 kg.",
  "instance": "/api/v1/servicing-order/initiation",
  "errors": [
    {
      "field": "items[1].weightKg",
      "message": "Debe ser mayor que 0 y como máximo 500"
    }
  ]
}
```

### GET `/servicing-order/{cr-reference-id}/acceptance/retrieval`

**Ver quiénes aceptaron mi pedido (vecino)** · `operationId: retrieveServicingOrderAcceptance`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Acceptance** · actionTerm: **Retrieve**

HU-13. Lista de recicladores que aceptaron (nombre, celular, fecha y hora), del primero al último en aceptar.
Solo el vecino dueño.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Aceptaciones del pedido | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |

Respuesta 200:

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `items` | array de [AcceptanceRecord](#schema-acceptancerecord) | sí |  |

Ejemplo `dos_aceptaciones`: Aceptaciones del pedido P, de la primera a la última

```json
{
  "items": [
    {
      "id": "3c9a7e15-d2b4-4f68-9a01-5e8b2d4c6f97",
      "status": "ACCEPTED",
      "fullName": "Jorge Luis Mendoza Ruiz",
      "phone": "944123456",
      "acceptedAt": "2026-10-26T15:10:00Z"
    },
    {
      "id": "8e1d5b72-4a9c-4e30-b6f8-0c7a3d9e1b45",
      "status": "ACCEPTED",
      "fullName": "Marleni Quispe Rojas",
      "phone": "955876543",
      "acceptedAt": "2026-10-26T16:40:00Z"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

### PUT `/servicing-order/{cr-reference-id}/confirmation/execution`

**Elegir un reciclador (vecino)** · `operationId: executeServicingOrderConfirmation`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Confirmation** · actionTerm: **Execute**

HU-13 (RN12). En una sola transacción: el pedido pasa a `CONFIRMED`, la aceptación elegida a `CHOSEN` y las demás a `DISCARDED`.
La elección no se puede cambiar. Si el pedido ya no está `PENDING` (doble toque, o cancelado), 409 `ORDER_NOT_PENDING`.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Cuerpo de la solicitud** (obligatorio)

`application/json`

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `acceptanceId` | string (uuid) | sí |  |

Ejemplo `elegir`: El vecino elige la aceptación de Jorge sobre el pedido Q

```json
{
  "acceptanceId": "9f3b6d12-7a84-4c05-b1e9-2d5c8a0e4f73"
}
```

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Reciclador elegido | `application/json` |
| 400 | Datos inválidos (`VALIDATION_ERROR`) | `application/problem+json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido ya no está `PENDING` (`ORDER_NOT_PENDING`) | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `confirmado`: Pedido CONFIRMED; las demás aceptaciones pasan a DISCARDED

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "CONFIRMED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 2,
    "acceptanceStatus": null,
    "chosenRecyclerName": "Jorge Luis Mendoza Ruiz",
    "counterpart": {
      "fullName": "Jorge Luis Mendoza Ruiz",
      "phone": "944123456"
    },
    "pickup": null,
    "availableActions": [
      "MARK_COLLECTED",
      "CANCEL"
    ],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": null,
    "cancelledAt": null
  }
}
```

Error 400 `validacion`: Contraseña muy corta

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/initiation",
  "errors": [
    {
      "field": "password",
      "message": "Debe tener al menos 8 caracteres"
    }
  ]
}
```

Error 400 `celular`: Celular con formato inválido

```json
{
  "type": "https://api.reciclatru.example.com/errors/validation-error",
  "title": "Datos inválidos",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "detail": "Revisa los campos indicados.",
  "instance": "/api/v1/party-data-management/me/update",
  "errors": [
    {
      "field": "phone",
      "message": "Debe tener 9 dígitos y empezar con 9"
    }
  ]
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_pendiente`: Doble toque o pedido cancelado

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-pending",
  "title": "El pedido ya no está pendiente",
  "status": 409,
  "code": "ORDER_NOT_PENDING",
  "detail": "Ya elegiste un reciclador o el pedido fue cancelado.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/confirmation/execution"
}
```

### PUT `/servicing-order/{cr-reference-id}/fulfillment/execution`

**Marcar como Recolectado (vecino)** · `operationId: executeServicingOrderFulfillment`

BIAN: serviceDomain: **Servicing Order** · behaviorQualifier: **Fulfillment** · actionTerm: **Execute**

HU-16 (RN15). Solo el vecino dueño y solo si el pedido está `CONFIRMED`; si no, 409 `ORDER_NOT_CONFIRMED`.
Estado final: no se deshace. La aceptación del elegido no cambia. Desde ese momento el reciclador deja de ver
la dirección y el teléfono. Un doble toque marca el pedido una sola vez.

Autenticación: Bearer JWT

**Parámetros**

| Nombre | En | Req. | Tipo | Descripción |
|---|---|---|---|---|
| `cr-reference-id` | path | sí | string | Referencia del Control Record (BIAN). En Party Data Management y Location Data Management acepta el alias `me`. |

**Respuestas**

| Código | Descripción | Contenido |
|---|---|---|
| 200 | Pedido recolectado | `application/json` |
| 401 | Token ausente, inválido o vencido, o credenciales incorrectas (`UNAUTHENTICATED`) | `application/problem+json` |
| 403 | Sin permiso. Códigos: `FORBIDDEN_ROLE`, `NOT_ORDER_OWNER`, `NOT_CHOSEN_RECYCLER`. | `application/problem+json` |
| 404 | Recurso inexistente (`NOT_FOUND`) | `application/problem+json` |
| 409 | El pedido no está `CONFIRMED` (`ORDER_NOT_CONFIRMED`) | `application/problem+json` |

Respuesta 200:

Esquema: [ServicingOrderOutputModel](#schema-servicingorderoutputmodel)

Ejemplo `recolectado`: Pedido COLLECTED (final)

```json
{
  "crReferenceId": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
  "servicingOrderInstanceRecord": {
    "id": "5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24",
    "status": "COLLECTED",
    "items": [
      {
        "description": "Botellas de vidrio",
        "weightKg": 6.0
      },
      {
        "description": "Periódicos y revistas",
        "weightKg": 5.5
      }
    ],
    "totalWeightKg": 11.5,
    "photos": [
      {
        "id": "d6e7f809-1a23-4b34-95d6-f7a8091a2b42",
        "url": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/photo/d6e7f809-1a23-4b34-95d6-f7a8091a2b42/retrieval"
      }
    ],
    "zone": "Huanchaco",
    "approximateDistanceKm": null,
    "acceptanceCount": 2,
    "acceptanceStatus": null,
    "chosenRecyclerName": null,
    "counterpart": null,
    "pickup": null,
    "availableActions": [],
    "createdAt": "2026-10-23T14:10:00Z",
    "confirmedAt": "2026-10-24T16:30:00Z",
    "collectedAt": "2026-10-26T20:15:00Z",
    "cancelledAt": null
  }
}
```

Error 401 `token`: Token ausente, inválido o vencido: la app borra el token y vuelve al inicio

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Sesión no válida",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Token ausente, inválido o vencido.",
  "instance": "/api/v1/servicing-order/retrieval"
}
```

Error 401 `credenciales`: Login con datos incorrectos (no indica cuál falló)

```json
{
  "type": "https://api.reciclatru.example.com/errors/unauthenticated",
  "title": "Credenciales incorrectas",
  "status": 401,
  "code": "UNAUTHENTICATED",
  "detail": "Correo o contraseña incorrectos.",
  "instance": "/api/v1/party-authentication/initiation"
}
```

Error 403 `rol`: Rol sin permiso

```json
{
  "type": "https://api.reciclatru.example.com/errors/forbidden-role",
  "title": "Acción no permitida para tu rol",
  "status": 403,
  "code": "FORBIDDEN_ROLE",
  "detail": "Solo un vecino puede publicar pedidos.",
  "instance": "/api/v1/servicing-order/initiation"
}
```

Error 403 `no_dueno`: No es el dueño del pedido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-order-owner",
  "title": "No eres el dueño del pedido",
  "status": 403,
  "code": "NOT_ORDER_OWNER",
  "detail": "Solo el vecino que publicó el pedido puede hacer esto.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/update"
}
```

Error 403 `no_elegido`: No es el reciclador elegido

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-chosen-recycler",
  "title": "No eres el reciclador elegido",
  "status": 403,
  "code": "NOT_CHOSEN_RECYCLER",
  "detail": "Solo el reciclador elegido puede ver la dirección exacta.",
  "instance": "/api/v1/servicing-order/5b2e9d41-8c37-4f06-a1d8-7e3c6b9f0a24/retrieval"
}
```

Error 404 `no_encontrado`: El recurso no existe

```json
{
  "type": "https://api.reciclatru.example.com/errors/not-found",
  "title": "No encontrado",
  "status": 404,
  "code": "NOT_FOUND",
  "detail": "El recurso solicitado no existe.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/retrieval"
}
```

Error 409 `no_confirmado`: El pedido no está CONFIRMED

```json
{
  "type": "https://api.reciclatru.example.com/errors/order-not-confirmed",
  "title": "El pedido no está confirmado",
  "status": 409,
  "code": "ORDER_NOT_CONFIRMED",
  "detail": "Solo se puede marcar como recolectado un pedido confirmado.",
  "instance": "/api/v1/servicing-order/0f8a3c52-6d1e-4b7a-9e40-2c5d8b1a7f36/fulfillment/execution"
}
```

---

## Esquemas

### Schema: PartyRole <a id="schema-partyrole"></a>

Valores: `NEIGHBOR`, `RECYCLER`

### Schema: OrderStatus <a id="schema-orderstatus"></a>

`PENDING` → `CONFIRMED` → `COLLECTED`. Desde `PENDING` o `CONFIRMED` se puede pasar a `CANCELLED`.
`COLLECTED` y `CANCELLED` son estados finales.

Valores: `PENDING`, `CONFIRMED`, `COLLECTED`, `CANCELLED`

### Schema: AcceptanceStatus <a id="schema-acceptancestatus"></a>

Nace `ACCEPTED` y termina en `CHOSEN` o `DISCARDED`.

Valores: `ACCEPTED`, `CHOSEN`, `DISCARDED`

### Schema: AcceptanceLabel <a id="schema-acceptancelabel"></a>

Etiqueta que ve el reciclador en «Mis pedidos aceptados». Combina el estado de la aceptación y el del pedido.

Valores: `AWAITING_CONFIRMATION`, `CHOSEN`, `NOT_CHOSEN`, `CANCELLED_BY_NEIGHBOR`, `COLLECTED`

### Schema: PartyDataManagementInitiateInputModel <a id="schema-partydatamanagementinitiateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `email` | string (email) | sí |  |
| `password` | string (password) · long. mín 8 | sí |  |
| `role` | [PartyRole](#schema-partyrole) | sí |  |

### Schema: PartyDataManagementUpdateInputModel <a id="schema-partydatamanagementupdateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `fullName` | string · long. mín 2, long. máx 80 | no | Nombre y apellido. |
| `phone` | string · patrón `^9\d{8}$` | no | 9 dígitos, empieza con 9. |
| `reference` | string · long. máx 150 | no | Solo vecino. Referencia opcional para ubicar la puerta. |

### Schema: PickupPointUpdateInputModel <a id="schema-pickuppointupdateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `latitude` | number (double) · mín -90, máx 90 | sí | Redondeada a 6 decimales. |
| `longitude` | number (double) · mín -180, máx 180 | sí |  |
| `address` | string | no | Dirección aproximada; el vecino puede corregirla o escribirla a mano. El punto del mapa no cambia. (nullable) |
| `district` | string | no | (nullable) |

### Schema: PartyAuthenticationInitiateInputModel <a id="schema-partyauthenticationinitiateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `email` | string (email) | sí |  |
| `password` | string (password) | sí |  |

### Schema: PartyAuthenticationOutputModel <a id="schema-partyauthenticationoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `crReferenceId` | string (uuid) | sí |  |
| `accessToken` | string | sí |  |
| `tokenType` | string | no |  |
| `expiresAt` | string (date-time) | sí | Emisión + 30 días. |
| `partyDataManagementInstanceRecord` | [PartyDataManagementInstanceRecord](#schema-partydatamanagementinstancerecord) | sí |  |

### Schema: PartyDataManagementInstanceRecord <a id="schema-partydatamanagementinstancerecord"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `id` | string (uuid) | sí |  |
| `email` | string (email) | sí | No editable. |
| `role` | [PartyRole](#schema-partyrole) | sí |  |
| `fullName` | string | no | (nullable) |
| `phone` | string | no | (nullable) |
| `reference` | string | no | (nullable) |
| `pickupPoint` | object | no | (nullable) |
| `profileComplete` | boolean | sí | Vecino con nombre, celular y ubicación; reciclador con nombre y celular (RN20). |
| `createdAt` | string (date-time) | sí |  |

### Schema: PartyDataManagementOutputModel <a id="schema-partydatamanagementoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `crReferenceId` | string (uuid) | sí |  |
| `partyDataManagementInstanceRecord` | [PartyDataManagementInstanceRecord](#schema-partydatamanagementinstancerecord) | sí |  |

### Schema: PickupPoint <a id="schema-pickuppoint"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `latitude` | number (double) | sí |  |
| `longitude` | number (double) | sí |  |
| `address` | string | no | (nullable) |
| `district` | string | no | (nullable) |

### Schema: OrderItem <a id="schema-orderitem"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `description` | string · long. mín 1, long. máx 100 | sí |  |
| `weightKg` | number (double) · mín 0, máx 500, > True | sí | Peso estimado en kg, con un decimal como máximo (RN06). |

### Schema: ServicingOrderInitiateInputModel <a id="schema-servicingorderinitiateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `items` | array de [OrderItem](#schema-orderitem) | sí |  |

### Schema: ServicingOrderUpdateInputModel <a id="schema-servicingorderupdateinputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `items` | array de [OrderItem](#schema-orderitem) | sí |  |

### Schema: OrderPhoto <a id="schema-orderphoto"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `id` | string (uuid) | sí |  |
| `url` | string | sí | Ruta de `.../photo/{photo-id}/retrieval`. |

### Schema: Counterpart <a id="schema-counterpart"></a>

Contacto de la otra parte. Solo mientras el pedido esté `PENDING` o `CONFIRMED` y exista una aceptación `ACCEPTED` o `CHOSEN`.

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `fullName` | string | sí |  |
| `phone` | string | sí |  |

### Schema: PickupDetail <a id="schema-pickupdetail"></a>

Solo para el reciclador elegido, mientras el pedido esté `CONFIRMED` (RN13).

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `address` | string | no | Calle y distrito. Si es nulo, la app muestra «Sin dirección escrita». (nullable) |
| `reference` | string | no | (nullable) |
| `latitude` | number (double) | sí |  |
| `longitude` | number (double) | sí | Con estas coordenadas la app arma la URI `geo:` de «Cómo llegar». |

### Schema: ServicingOrderInstanceRecord <a id="schema-servicingorderinstancerecord"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `id` | string (uuid) | sí |  |
| `status` | [OrderStatus](#schema-orderstatus) | sí | `PENDING` → `CONFIRMED` → `COLLECTED`. Desde `PENDING` o `CONFIRMED` se puede pasar a `CANCELLED`. `COLLECTED` y `CANCELLED` son estados finales. |
| `items` | array de [OrderItem](#schema-orderitem) | sí |  |
| `totalWeightKg` | number (double) | sí |  |
| `photos` | array de [OrderPhoto](#schema-orderphoto) | no |  |
| `zone` | string | no | Distrito del punto de recojo. (nullable) |
| `approximateDistanceKm` | number (double) | no | Solo para el reciclador y solo si envió su ubicación. Nunca se guarda. (nullable) |
| `acceptanceCount` | integer | no | Cantidad de recicladores que aceptaron. |
| `alreadyAccepted` | boolean | no | Solo reciclador. |
| `acceptanceStatus` | object | no | Estado de la aceptación del reciclador que consulta. (nullable) |
| `chosenRecyclerName` | string | no | Solo vecino, con el pedido `CONFIRMED`. (nullable) |
| `counterpart` | object | no | (nullable) |
| `pickup` | object | no | (nullable) |
| `availableActions` | array de enum: `EDIT`, `CANCEL`, `CHOOSE_RECYCLER`, `MARK_COLLECTED`, `ACCEPT`, `GO_PICKUP` | no | Acciones posibles en ese estado para quien consulta. |
| `createdAt` | string (date-time) | sí |  |
| `confirmedAt` | string (date-time) | no | (nullable) |
| `collectedAt` | string (date-time) | no | (nullable) |
| `cancelledAt` | string (date-time) | no | (nullable) |

### Schema: ServicingOrderOutputModel <a id="schema-servicingorderoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `crReferenceId` | string (uuid) | sí |  |
| `servicingOrderInstanceRecord` | [ServicingOrderInstanceRecord](#schema-servicingorderinstancerecord) | sí |  |

### Schema: ServicingOrderSummary <a id="schema-servicingordersummary"></a>

Resumen liviano para listas. Nunca incluye dirección, nombre ni teléfono (salvo el nombre del reciclador elegido para el vecino dueño).

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `id` | string (uuid) | sí |  |
| `status` | [OrderStatus](#schema-orderstatus) | sí | `PENDING` → `CONFIRMED` → `COLLECTED`. Desde `PENDING` o `CONFIRMED` se puede pasar a `CANCELLED`. `COLLECTED` y `CANCELLED` son estados finales. |
| `itemCount` | integer | sí |  |
| `totalWeightKg` | number (double) | sí |  |
| `thumbnailUrl` | string | no | Miniatura de la primera foto, si tiene. (nullable) |
| `zone` | string | no | (nullable) |
| `approximateDistanceKm` | number (double) | no | (nullable) |
| `acceptanceCount` | integer | no | Solo vecino con el pedido `PENDING`. (nullable) |
| `chosenRecyclerName` | string | no | (nullable) |
| `alreadyAccepted` | boolean | no | (nullable) |
| `createdAt` | string (date-time) | sí |  |

### Schema: ServicingOrderListOutputModel <a id="schema-servicingorderlistoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `items` | array de [ServicingOrderSummary](#schema-servicingordersummary) | sí |  |
| `nextCursor` | string | no | (nullable) |

### Schema: ServicingOrderAvailabilityOutputModel <a id="schema-servicingorderavailabilityoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `sortedBy` | enum: `DISTANCE`, `DATE` | sí | `DATE` cuando no se envió ubicación (la app muestra «Activa tu ubicación para ver la distancia»). |
| `items` | array de [ServicingOrderSummary](#schema-servicingordersummary) | sí |  |
| `nextCursor` | string | no | (nullable) |

### Schema: MyAcceptanceSummary <a id="schema-myacceptancesummary"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `orderId` | string (uuid) | sí |  |
| `acceptanceLabel` | [AcceptanceLabel](#schema-acceptancelabel) | sí | Etiqueta que ve el reciclador en «Mis pedidos aceptados». Combina el estado de la aceptación y el del pedido. |
| `zone` | string | no | (nullable) |
| `totalWeightKg` | number (double) | sí |  |
| `acceptedAt` | string (date-time) | sí |  |

### Schema: MyAcceptanceListOutputModel <a id="schema-myacceptancelistoutputmodel"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `items` | array de [MyAcceptanceSummary](#schema-myacceptancesummary) | sí |  |
| `nextCursor` | string | no | (nullable) |

### Schema: AcceptanceRecord <a id="schema-acceptancerecord"></a>

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `id` | string (uuid) | sí |  |
| `status` | [AcceptanceStatus](#schema-acceptancestatus) | sí | Nace `ACCEPTED` y termina en `CHOSEN` o `DISCARDED`. |
| `fullName` | string | sí |  |
| `phone` | string | sí |  |
| `acceptedAt` | string (date-time) | sí |  |

### Schema: Problem <a id="schema-problem"></a>

Error según RFC 7807.

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `type` | string | sí |  |
| `title` | string | sí |  |
| `status` | integer | sí |  |
| `code` | string | sí |  |
| `detail` | string | no |  |
| `instance` | string | no |  |
| `correlationId` | string | no | Valor de `x-correlation-id` de la petición; sirve para rastrear el error en los registros. |
| `errors` | array de object | no | Detalle por campo en errores de validación. |
