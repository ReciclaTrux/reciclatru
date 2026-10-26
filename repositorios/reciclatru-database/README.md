# reciclatru-db

Repositorio de la base de datos de **ReciclaTru** (PostgreSQL en Neon). Versiona el esquema con **Flyway**, separado del backend NestJS/TypeORM.

## Estructura

```
reciclatru-db/
├─ migrations/                         migraciones de Flyway (una por cambio)
│  └─ V20261026_1530__initial_schema.sql
├─ seeds/dev_seed.sql                  datos de prueba, solo local
├─ docs/data-model.md                  diagrama Mermaid y reglas de negocio
├─ docker-compose.yml                  PostgreSQL local + Flyway
├─ flyway.conf                         configuración común (sin secretos)
├─ .env.example                        variables de conexión de ejemplo
└─ .github/workflows/
   ├─ validate.yml                     PR: aplica todo desde cero y valida
   └─ deploy-neon.yml                  main: migra Neon
```

## Uso local

```bash
docker compose up -d db              # PostgreSQL 16
docker compose run --rm flyway       # aplica las migraciones
docker compose exec -T db psql -U reciclatru -d reciclatru < seeds/dev_seed.sql   # opcional
docker compose down -v               # reinicia desde cero
```

## Convención de migraciones

- Nombre: `V<AAAAMMDD_HHMM>__descripcion_en_snake_case.sql`. La fecha y la hora evitan choques cuando dos ramas crean "la siguiente versión" a la vez.
- **Nunca se edita una migración ya integrada a `main`.** Cualquier cambio es una migración nueva (Flyway valida el checksum y falla si cambió).
- Una migración por cambio lógico. Flyway ya ejecuta cada una en una transacción: no pongas `BEGIN`/`COMMIT`.
- Un `ALTER TYPE ... ADD VALUE` va **solo** en su archivo: el valor nuevo no puede usarse en la misma transacción. Se usa en una migración posterior.
- Los cambios deben ser **compatibles hacia atrás** con el backend desplegado: una columna nueva entra `NULL`; recién cuando el backend la usa se vuelve `NOT NULL` en otra migración.
- No hay deshacer automático (la edición gratuita no lo incluye): se corrige hacia adelante con una migración nueva.

## Flujo en Git

1. Rama por cambio: `git switch -c feat/add-expired-status`.
2. Crear la migración en `migrations/` y probarla en local.
3. Pull request hacia `main`: el workflow **Validate migrations** aplica todo sobre una base vacía y corre `validate`.
4. Revisión de otro integrante y merge.
5. El workflow **Deploy to Neon** migra Neon (con aprobación manual si activas reglas en el environment `production`).

## Neon

- Usa la cadena de conexión **directa** (sin `-pooler`) para las migraciones: Flyway usa bloqueos de sesión que el pooler puede romper.
- Formato JDBC: `jdbc:postgresql://<host>/<base>?sslmode=require`.
- Secrets del repositorio en GitHub (Settings → Secrets and variables → Actions): `NEON_FLYWAY_URL`, `NEON_FLYWAY_USER`, `NEON_FLYWAY_PASSWORD`. Crea el environment `production`.
- El backend NestJS sí puede usar la cadena **pooled** de Neon para la aplicación.
- Primera ejecución contra Neon: la base debe estar vacía; Flyway creará `flyway_schema_history`.

## Backend (NestJS + TypeORM)

- `synchronize: false` y sin `migrationsRun`: el backend no modifica el esquema.
- Las columnas enum declaran `enumName` con el nombre del tipo existente (`order_status_type`, etc.).
- `numeric` llega como `string`: usar `transformer` en `weight_kg`, `latitude` y `longitude`.
- En las pruebas e2e del backend, levantar la base con las migraciones de este repositorio para detectar desajustes entre entidades y esquema.

## Primer push

```bash
cd reciclatru-db
git init -b main
git add .
git commit -m "chore: esquema inicial de ReciclaTru con Flyway"
git remote add origin <url-del-repositorio>
git push -u origin main
```
