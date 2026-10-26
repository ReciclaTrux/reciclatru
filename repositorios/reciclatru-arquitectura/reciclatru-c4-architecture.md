# ReciclaTru: arquitectura C4 (niveles 1 y 2)

Modelo C4 de la iniciativa. Los diagramas están en Mermaid (`flowchart` con estilo C4) y se ven directamente en GitHub, GitLab y la mayoría de visores de Markdown.

## Nivel 1: Contexto del sistema

Muestra quién usa ReciclaTru y con qué sistemas externos se relaciona.

```mermaid
flowchart TB
    neighbor(["<b>Vecino</b><br/>[Persona]<br/>Publica pedidos de recojo de material<br/>reciclable y confirma al reciclador"]):::person
    recycler(["<b>Reciclador</b><br/>[Persona]<br/>Busca pedidos cercanos, los acepta<br/>y recoge el material"]):::person
    rt["<b>ReciclaTru</b><br/>[Sistema]<br/>Conecta vecinos y recicladores de Huanchaco<br/>y Trujillo para coordinar el recojo"]:::system
    location["<b>Servicios de ubicación del dispositivo</b><br/>[Sistema externo]<br/>GPS del teléfono Android"]:::ext

    neighbor -->|"Crea y gestiona pedidos,<br/>adjunta fotos<br/>[App móvil]"| rt
    recycler -->|"Busca y acepta pedidos,<br/>marca la recolección<br/>[App móvil]"| rt
    rt -->|"Obtiene la posición actual<br/>[API de Android]"| location

    classDef person fill:#08427b,stroke:#052e56,color:#fff
    classDef system fill:#1168bd,stroke:#0b4884,color:#fff
    classDef ext fill:#999,stroke:#6b6b6b,color:#fff
```

**Notas**

- Hay dos tipos de usuario y cada cuenta tiene un único rol (RN01).
- La posición del reciclador se usa solo para buscar pedidos cercanos y no se guarda (RN19).
- La dirección exacta del vecino solo es visible para el reciclador elegido, con el pedido confirmado (RN13).
- Por ahora no hay otros sistemas externos de negocio (pagos, mensajería, notificaciones push): son ampliaciones del Release 2.

## Nivel 2: Contenedores

Muestra las piezas desplegables y cómo se comunican.

```mermaid
flowchart LR
    neighbor(["<b>Vecino</b><br/>[Persona]<br/>Publica pedidos"]):::person
    recycler(["<b>Reciclador</b><br/>[Persona]<br/>Acepta y recoge pedidos"]):::person
    location["<b>Servicios de ubicación</b><br/>[Sistema externo]<br/>GPS del dispositivo"]:::ext

    subgraph rt ["ReciclaTru [Sistema]"]
        direction LR
        app["<b>App móvil Android</b><br/>[Kotlin, Jetpack Compose, Coil]<br/>Pedidos, fotos, búsqueda por<br/>cercanía, aceptaciones"]:::container
        api["<b>API REST</b><br/>[NestJS, TypeORM, Render]<br/>Reglas RN01-RN20, JWT,<br/>rutas BIAN, URLs firmadas"]:::container
        db[("<b>Base de datos</b><br/>[PostgreSQL en Neon]<br/>Usuarios, ubicaciones, pedidos,<br/>aceptaciones, historial, clave de fotos")]:::store
        storage[("<b>Almacenamiento de fotos</b><br/>[Neon Object Storage, bucket privado]<br/>Imágenes JPEG y PNG de los pedidos")]:::store
        mig["<b>Repositorio de migraciones</b><br/>[Flyway, GitHub Actions]<br/>Versiona y aplica el esquema"]:::container
    end

    neighbor -->|Usa| app
    recycler -->|Usa| app
    app -->|"Lee la posición<br/>[API de Android]"| location
    app -->|"Consume<br/>[HTTPS, JSON, JWT]"| api
    api -->|"Lee y escribe<br/>[TypeORM, SSL, pooled]"| db
    api -->|"Sube, borra y firma URLs<br/>[API S3, HTTPS]"| storage
    app -.->|"Descarga la foto (redirección 302)<br/>[HTTPS, URL firmada de 5 min]"| storage
    mig -->|"Aplica migraciones<br/>[JDBC, conexión directa]"| db

    classDef person fill:#08427b,stroke:#052e56,color:#fff
    classDef container fill:#438dd5,stroke:#2e6295,color:#fff
    classDef store fill:#2e6295,stroke:#1f4366,color:#fff
    classDef ext fill:#999,stroke:#6b6b6b,color:#fff
    style rt fill:#fff,stroke:#444,stroke-dasharray:5 5
```

## Contenedores

| Contenedor | Tecnología | Responsabilidad | Despliegue |
|---|---|---|---|
| App móvil Android | Kotlin, Jetpack Compose, Coil | Interfaz de vecino y reciclador; comprime fotos a ~300 KB; sigue la redirección 302 para mostrar fotos; usa el GPS para buscar pedidos cercanos | Dispositivo del usuario |
| API REST | NestJS, TypeORM (`synchronize: false`) | Valida las reglas de negocio, autentica con JWT, sirve las rutas BIAN, valida y sube las fotos, responde 302 hacia URLs firmadas | Render (Azure en evaluación) |
| Base de datos | PostgreSQL (Neon) | Persistencia, ENUM nativos, índices parciales, función `confirm_pickup_order`, cálculo Haversine | Neon, proyecto `reciclatru`, AWS US East 2 |
| Almacenamiento de fotos | Neon Object Storage (S3), bucket privado | Guarda las imágenes; la base solo guarda la clave | Mismo proyecto de Neon |
| Repositorio de migraciones | Flyway, GitHub Actions | Versiona y despliega el esquema; el backend no lo modifica | Repositorio `reciclatru-db` |

## Protocolos y seguridad

- **App → API:** HTTPS; `Authorization: Bearer <accessToken>`; JSON y `multipart/form-data` para las fotos.
- **API → base de datos:** cadena **pooled** de Neon, SSL obligatorio. Una violación de serialización (SQLSTATE 40001) se traduce a `409`.
- **Migraciones → base de datos:** cadena **directa** (sin `-pooler`), porque Flyway usa bloqueos de sesión. Las credenciales viven como secretos del environment `production` en GitHub.
- **API → almacenamiento:** API S3 con credenciales de lectura y escritura, solo como secretos de Render.
- **App → almacenamiento:** solo mediante URL firmada de corta duración que entrega el API en una redirección 302. La app nunca recibe una URL permanente ni credenciales.

## Decisiones reflejadas

1. **Base de datos y fotos en el mismo proveedor (Neon):** una cuenta, un conjunto de credenciales y una región.
2. **Esquema versionado fuera del backend:** el repositorio de migraciones es independiente; el backend no ejecuta migraciones.
3. **Fotos fuera del servidor:** el disco de Render es efímero; la base guarda solo la clave del objeto.
4. **Interfaz `PhotoStorage` dentro del API:** permite pasar a Cloudflare R2 o Azure Blob con una variable de entorno.
5. **Monolito modular en el API:** una sola unidad desplegable, con módulos por dominio BIAN. Es lo adecuado para un equipo de 7 y para el plazo del curso.

## Pendientes y supuestos

- **Servicios de ubicación:** se asume el GPS del dispositivo para HU de búsqueda por cercanía. Si el informe prevé mapas o navegación (por ejemplo, abrir Google Maps), se añade como sistema externo.
- **Alojamiento del API:** Render por ahora. Si se aprueba la cuenta educativa de Azure, solo cambia el despliegue del contenedor API.
- **Notificaciones push (FCM)** y el **refresh token** (tabla `auth_session`) no están en este diagrama: se añaden en el Release 2.
- El estado de Neon Object Storage (preview o estable) no está indicado en su documentación; conviene revisarlo antes de depender de él más allá del curso.

## Nivel 3: Componentes del API REST

Descompone el contenedor **API REST** (NestJS) en sus módulos. Cada módulo de NestJS es un componente y sigue el patrón controller, service y repository (TypeORM). Las flechas indican dependencias entre módulos.

```mermaid
flowchart TB
    app["<b>App móvil Android</b><br/>[Contenedor]"]:::ext
    db[("<b>PostgreSQL en Neon</b><br/>[Contenedor]")]:::store
    storage[("<b>Neon Object Storage</b><br/>[Contenedor]")]:::store

    subgraph api ["API REST [NestJS]"]
        direction TB

        subgraph common ["common (transversal)"]
            direction LR
            guards["<b>JwtAuthGuard y RolesGuard</b><br/>Valida el token y el rol<br/>(RN01, RN02)"]:::comp
            filter["<b>ProblemDetailsFilter</b><br/>Errores problem+json y<br/>SQLSTATE 40001 a 409"]:::comp
            pipes["<b>ValidationPipe y DTOs</b><br/>Valida entradas según el OpenAPI"]:::comp
        end

        subgraph auth ["party-authentication"]
            authc["<b>AuthController</b><br/>/party-authentication/initiation"]:::comp
            auths["<b>AuthService</b><br/>Verifica la clave, emite el JWT"]:::comp
        end

        subgraph party ["party-data-management"]
            partyc["<b>PartyController</b><br/>Registro, consulta y perfil<br/>(HU-01, HU-03)"]:::comp
            partys["<b>PartyService</b><br/>Rol y correo inmutables<br/>perfil completo (RN20)"]:::comp
        end

        subgraph loc ["location-data-management"]
            locc["<b>LocationController</b><br/>{id}/pickup-point/update"]:::comp
            locs["<b>LocationService</b><br/>Punto de recojo del vecino"]:::comp
        end

        subgraph order ["servicing-order"]
            orderc["<b>OrderController</b><br/>Crear, listar, editar, cancelar"]:::comp
            accc["<b>AcceptanceController</b><br/>Aceptar, confirmar, recolectar"]:::comp
            photoc["<b>PhotoController</b><br/>Subir, ver y quitar fotos<br/>(HU-09)"]:::comp
            orders["<b>OrderService</b><br/>Ítems 1-10, peso, snapshot de<br/>ubicación (RN18), cancelación lógica"]:::comp
            accs["<b>AcceptanceService</b><br/>Aceptación múltiple, confirmación<br/>vía confirm_pickup_order, RN13"]:::comp
            avail["<b>AvailabilityQuery</b><br/>Pedidos cercanos con Haversine,<br/>posición del reciclador no guardada (RN19)"]:::comp
            hist["<b>StatusHistoryRecorder</b><br/>Historial de estados"]:::comp
        end

        subgraph photo ["photo-storage"]
            iface["<b>PhotoStorage</b><br/>[Interfaz: put, delete, getSignedUrl]"]:::comp
            s3["<b>S3PhotoStorage</b><br/>@aws-sdk/client-s3<br/>(Neon, R2)"]:::comp
            az["<b>AzureBlobPhotoStorage</b><br/>@azure/storage-blob<br/>(opcional)"]:::opt
        end

        orm["<b>TypeORM</b><br/>Entidades y repositorios<br/>synchronize: false"]:::comp
    end

    app -->|"HTTPS, JSON, JWT"| authc
    app --> partyc
    app --> locc
    app --> orderc
    app --> accc
    app --> photoc

    authc --> auths
    partyc --> partys
    locc --> locs
    orderc --> orders
    accc --> accs
    accc --> avail
    photoc --> orders

    auths --> partys
    orders --> partys
    orders --> locs
    orders --> hist
    accs --> hist
    orders --> iface
    iface -.-> s3
    iface -.-> az

    partys --> orm
    locs --> orm
    orders --> orm
    accs --> orm
    avail --> orm
    hist --> orm
    auths --> orm

    orm -->|"SQL, SSL, pooled"| db
    s3 -->|"API S3, URLs firmadas"| storage

    classDef comp fill:#85bbf0,stroke:#5d82a8,color:#000
    classDef opt fill:#d6e6f7,stroke:#5d82a8,color:#000,stroke-dasharray:4 4
    classDef ext fill:#438dd5,stroke:#2e6295,color:#fff
    classDef store fill:#2e6295,stroke:#1f4366,color:#fff
```

**Lectura del diagrama**

- Los guards, el filtro de errores y los pipes de `common` se aplican globalmente a todos los controladores; no se dibujan flechas para no saturar.
- `servicing-order` es el módulo central: concentra las reglas de pedidos, aceptaciones y ubicación (hasta RN18). La confirmación del reciclador no se hace en memoria: `AcceptanceService` delega en la función `confirm_pickup_order` de la base, que es atómica y evita que dos vecinos o dos recicladores confirmen el mismo pedido.
- `photo-storage` expone solo la interfaz `PhotoStorage`; el proveedor concreto se elige con `STORAGE_DRIVER`. `servicing-order` nunca conoce el SDK de S3 ni el de Azure.
- Un módulo solo depende de los servicios públicos de otro, no de sus repositorios, para poder separarlos más adelante si hiciera falta.

## Nivel 4: Código (no se dibuja)

El modelo C4 lo define como opcional. Para ReciclaTru no se recomienda dibujarlo a mano, porque se desactualiza con cada commit. En su lugar:

- Diagrama de secuencia de la **confirmación del reciclador** (RN13, `confirm_pickup_order`, respuesta `409`).
- Diagrama de secuencia de la **subida y lectura de fotos** (bucket, clave del objeto, 302 a URL firmada).
- El detalle de entidades ya está en `docs/data-model.md`.

Si el curso exige un nivel 4, puede ser un diagrama de clases UML solo del módulo `servicing-order`.
