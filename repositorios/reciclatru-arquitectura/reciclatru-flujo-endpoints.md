# ReciclaTru API: orden de invocación de los endpoints

Respuesta corta: **primero se registra la cuenta (con su rol), después se hace login y ahí nace el token.** El login no crea nada: solo valida que la cuenta existe.

Rutas con prefijo `/api/v1`. Los endpoints marcados como públicos no llevan token; todos los demás requieren `Authorization: Bearer <accessToken>`.

## 1. Flujo de arranque (resuelve el 401 con la tabla vacía)

```mermaid
flowchart TD
    A([Inicio]) --> B[POST /party-data-management/initiation<br/>crea cuenta + rol]
    B --> C[POST /party-authentication/initiation<br/>devuelve accessToken]
    C --> D{¿Qué rol?}
    D -->|NEIGHBOR| E[PUT .../me/update<br/>nombre + celular]
    E --> F[PUT .../pickup-point/update<br/>ubicación]
    F --> G[POST /servicing-order/initiation<br/>publicar pedido]
    D -->|RECYCLER| H[PUT .../me/update<br/>nombre + celular]
    H --> I[GET /servicing-order/availability/retrieval<br/>ver pedidos]
    I --> J[POST .../acceptance/initiation<br/>aceptar]
    classDef pub fill:#e6f4e6,stroke:#2f7d32
    classDef auth fill:#e8eefb,stroke:#2f55a4
    class B,C pub
    class E,F,G,H,I,J auth
```

| Paso | Endpoint | Token | Qué hace |
|---|---|---|---|
| 1 | `POST /party-data-management/initiation` | No | Crea la cuenta con `email`, `password` y `role` (`NEIGHBOR` o `RECYCLER`). Responde 201 **sin token**. |
| 2 | `POST /party-authentication/initiation` | No | Valida correo y contraseña y devuelve `accessToken` (30 días). Con la cuenta inexistente responde 401. |
| 3 | `PUT /party-data-management/me/update` | Sí | Nombre y celular (el vecino puede agregar `reference`). |
| 4 | `PUT /location-data-management/me/pickup-point/update` | Sí (vecino) | Punto de recojo. Con esto el perfil del vecino queda completo. |

Reglas útiles:

- Un solo rol por cuenta y no se puede cambiar. Para probar las dos caras, crea dos cuentas con correos distintos.
- Para publicar pedidos, el vecino necesita nombre, celular **y** ubicación (si no, 422 `PROFILE_INCOMPLETE`). El reciclador necesita nombre y celular para aceptar.

## 2. Secuencia de registro, login y perfil

```mermaid
sequenceDiagram
    autonumber
    actor V as Vecino (app)
    actor R as Reciclador (app)
    participant API as ReciclaTru API

    rect rgb(235,245,235)
    Note over V,API: 1. Registro (público, sin token)
    V->>API: POST /party-data-management/initiation {email, password, role: NEIGHBOR}
    API-->>V: 201 cuenta creada (sin token)
    end

    rect rgb(235,240,250)
    Note over V,API: 2. Login (público): aquí nace el token
    V->>API: POST /party-authentication/initiation {email, password}
    API-->>V: 200 {accessToken, expiresAt (30 días)}
    end

    rect rgb(250,243,230)
    Note over V,API: 3. Completar perfil (Authorization: Bearer token)
    V->>API: PUT /party-data-management/me/update {fullName, phone, reference?}
    API-->>V: 200 perfil (profileComplete: false)
    V->>API: PUT /location-data-management/me/pickup-point/update {latitude, longitude, address, district}
    API-->>V: 200 perfil (profileComplete: true)
    end

    Note over R,API: El reciclador repite 1 y 2 con role: RECYCLER y completa solo nombre y celular
    R->>API: PUT /party-data-management/me/update {fullName, phone}
    API-->>R: 200 perfil (profileComplete: true)
```

## 3. Secuencia completa de un pedido

```mermaid
sequenceDiagram
    autonumber
    actor V as Vecino
    actor R as Reciclador
    participant API as ReciclaTru API

    Note over V,R: Ambos ya tienen cuenta, token y perfil completo

    V->>API: POST /servicing-order/initiation {items[1..10]}
    API-->>V: 201 pedido PENDING (crReferenceId = orderId)
    opt hasta 3 fotos
        V->>API: POST /servicing-order/{orderId}/photo/initiation (multipart file)
        API-->>V: 201 {id, url}
    end
    opt editar mientras no haya aceptaciones
        V->>API: PUT /servicing-order/{orderId}/update {items}
        API-->>V: 200
    end

    R->>API: GET /servicing-order/availability/retrieval?lat&lng
    API-->>R: 200 pedidos PENDING (sin datos personales)
    R->>API: POST /servicing-order/{orderId}/acceptance/initiation
    API-->>R: 201 aceptación ACCEPTED + contacto del vecino

    V->>API: GET /servicing-order/{orderId}/acceptance/retrieval
    API-->>V: 200 recicladores que aceptaron (nombre, celular)
    V->>API: PUT /servicing-order/{orderId}/confirmation/execution {acceptanceId}
    API-->>V: 200 pedido CONFIRMED

    R->>API: GET /servicing-order/{orderId}/retrieval
    API-->>R: 200 con dirección, referencia y coordenadas (elegido)

    V->>API: PUT /servicing-order/{orderId}/fulfillment/execution
    API-->>V: 200 pedido COLLECTED

    alt cancelar (PENDING o CONFIRMED)
        V->>API: PUT /servicing-order/{orderId}/termination
        API-->>V: 200 pedido CANCELLED
    end
```

## 4. Estados del pedido

`PENDING` → `CONFIRMED` (el vecino elige un reciclador) → `COLLECTED` (el vecino marca recolectado). Desde `PENDING` o `CONFIRMED` se puede pasar a `CANCELLED`. `COLLECTED` y `CANCELLED` son finales.

## 5. Errores frecuentes

| Respuesta | Causa típica |
|---|---|
| 401 `UNAUTHENTICATED` en el login | La cuenta no existe (falta el registro) o la contraseña es incorrecta. |
| 401 en cualquier otro endpoint | Falta el encabezado `Authorization: Bearer ...`, o el token venció o está alterado. |
| 403 `FORBIDDEN_ROLE` | El endpoint es de otro rol (por ejemplo, un reciclador publicando un pedido). |
| 422 `PROFILE_INCOMPLETE` | Falta completar nombre, celular o (vecino) la ubicación. |
| 409 | El pedido ya cambió de estado (aceptado, confirmado, cancelado). |
