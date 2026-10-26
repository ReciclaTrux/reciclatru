-- =============================================================================
-- ReciclaTru: esquema inicial (Release 1)
-- Requiere PostgreSQL 13 o superior (usa gen_random_uuid() nativo).
-- Naming en inglés, snake_case, tablas en singular.
--
-- Flyway ejecuta cada migración dentro de una transacción: este archivo NO lleva BEGIN/COMMIT.
-- Estados y rol: tipos ENUM de PostgreSQL (alineados con el OpenAPI v1.0).
-- Catálogo con tabla propia: solo `material`, porque crecerá y necesita nombre.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Tipos enumerados
-- -----------------------------------------------------------------------------

-- RN02: un solo rol por cuenta.
CREATE TYPE role_type AS ENUM ('NEIGHBOR', 'RECYCLER');

-- Ciclo de vida del pedido: PENDING -> CONFIRMED -> COLLECTED; CANCELLED desde PENDING o CONFIRMED.
-- Estados finales: COLLECTED y CANCELLED.
CREATE TYPE order_status_type AS ENUM ('PENDING', 'CONFIRMED', 'COLLECTED', 'CANCELLED');

-- Ciclo de vida de la aceptación: nace ACCEPTED y termina en CHOSEN o DISCARDED.
CREATE TYPE acceptance_status_type AS ENUM ('ACCEPTED', 'CHOSEN', 'DISCARDED');

-- Agregar valores después (Release 2: EXPIRED, WITHDRAWN) va en una migración propia:
--   ALTER TYPE order_status_type ADD VALUE 'EXPIRED';
--   ALTER TYPE acceptance_status_type ADD VALUE 'WITHDRAWN';
-- El valor nuevo no se puede usar en la misma transacción en que se agrega.

-- -----------------------------------------------------------------------------
-- 2. Catálogo de materiales (se usa desde el Release 2, HU-19)
-- -----------------------------------------------------------------------------

CREATE TABLE material (
    id    smallint    PRIMARY KEY,
    code  varchar(30) NOT NULL UNIQUE,
    name  varchar(50) NOT NULL
);

INSERT INTO material (id, code, name) VALUES
    (1, 'PAPER',     'Papel'),
    (2, 'CARDBOARD', 'Cartón'),
    (3, 'PLASTIC',   'Plástico'),
    (4, 'GLASS',     'Vidrio'),
    (5, 'METAL',     'Metal'),
    (6, 'OTHER',     'Otro');

-- -----------------------------------------------------------------------------
-- 3. Cuenta, perfil y ubicación
-- -----------------------------------------------------------------------------

CREATE TABLE app_user (
    id             uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    email          varchar(254) NOT NULL,
    password_hash  varchar(72)  NOT NULL,                 -- bcrypt
    role           role_type    NOT NULL,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_app_user_email_normalized CHECK (email = lower(btrim(email)))
);

-- RN03: correo único por cuenta, sin distinguir mayúsculas.
CREATE UNIQUE INDEX uq_app_user_email ON app_user (lower(email));

CREATE TABLE user_profile (
    user_id     uuid        PRIMARY KEY REFERENCES app_user (id) ON DELETE CASCADE,
    full_name   varchar(80) NOT NULL,
    phone       char(9)     NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_user_profile_full_name CHECK (char_length(btrim(full_name)) >= 2),
    CONSTRAINT ck_user_profile_phone     CHECK (phone ~ '^9[0-9]{8}$')   -- RN04
);

-- Instantánea inmutable del punto de recojo (RN18).
CREATE TABLE location (
    id              uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid          NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    latitude        numeric(9, 6) NOT NULL,
    longitude       numeric(9, 6) NOT NULL,
    address         varchar(200),
    district        varchar(80),
    reference_note  varchar(150),
    is_current      boolean       NOT NULL DEFAULT true,
    created_at      timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT ck_location_latitude  CHECK (latitude  BETWEEN  -90 AND  90),
    CONSTRAINT ck_location_longitude CHECK (longitude BETWEEN -180 AND 180),
    -- Permite la clave foránea compuesta desde pickup_order (el punto es del vecino).
    CONSTRAINT uq_location_id_user UNIQUE (id, user_id)
);

-- Una sola ubicación vigente por usuario.
CREATE UNIQUE INDEX uq_location_current_per_user ON location (user_id) WHERE is_current;
CREATE INDEX ix_location_user ON location (user_id);

-- -----------------------------------------------------------------------------
-- 4. Pedidos
-- -----------------------------------------------------------------------------

CREATE TABLE pickup_order (
    id                  uuid              PRIMARY KEY DEFAULT gen_random_uuid(),
    neighbor_id         uuid              NOT NULL REFERENCES app_user (id),
    pickup_location_id  uuid              NOT NULL,
    status              order_status_type NOT NULL DEFAULT 'PENDING',       -- RN08
    created_at          timestamptz       NOT NULL DEFAULT now(),
    updated_at          timestamptz       NOT NULL DEFAULT now(),
    confirmed_at        timestamptz,
    collected_at        timestamptz,
    cancelled_at        timestamptz,
    -- La ubicación del pedido debe pertenecer al mismo vecino que lo publica.
    CONSTRAINT fk_pickup_order_location
        FOREIGN KEY (pickup_location_id, neighbor_id) REFERENCES location (id, user_id),
    CONSTRAINT ck_pickup_order_collected CHECK (status <> 'COLLECTED' OR collected_at IS NOT NULL),
    CONSTRAINT ck_pickup_order_cancelled CHECK (status <> 'CANCELLED' OR cancelled_at IS NOT NULL),
    CONSTRAINT ck_pickup_order_confirmed CHECK (status NOT IN ('CONFIRMED', 'COLLECTED') OR confirmed_at IS NOT NULL)
);

-- Mis pedidos del vecino (HU-06), paginados de 20 en 20.
CREATE INDEX ix_pickup_order_neighbor ON pickup_order (neighbor_id, status, created_at DESC);
-- Lista de disponibles del reciclador (HU-10): solo Pendientes.
CREATE INDEX ix_pickup_order_pending ON pickup_order (created_at DESC) WHERE status = 'PENDING';

CREATE TABLE order_item (
    id           uuid          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     uuid          NOT NULL REFERENCES pickup_order (id) ON DELETE CASCADE,
    description  varchar(100)  NOT NULL,
    weight_kg    numeric(4, 1) NOT NULL,
    material_id  smallint      REFERENCES material (id),    -- opcional hasta el Release 2
    created_at   timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT ck_order_item_description CHECK (char_length(btrim(description)) >= 1),
    CONSTRAINT ck_order_item_weight      CHECK (weight_kg > 0 AND weight_kg <= 500)   -- RN06
);

CREATE INDEX ix_order_item_order ON order_item (order_id);

CREATE TABLE order_photo (
    id            uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id      uuid         NOT NULL REFERENCES pickup_order (id) ON DELETE CASCADE,
    file_name     varchar(80)  NOT NULL UNIQUE,            -- nombre único en /data/fotos
    sort_order    smallint     NOT NULL,
    content_type  varchar(30)  NOT NULL,
    size_bytes    integer      NOT NULL,
    created_at    timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_order_photo_sort_order CHECK (sort_order BETWEEN 1 AND 3),   -- RN07: máximo 3
    CONSTRAINT ck_order_photo_size       CHECK (size_bytes > 0),
    CONSTRAINT uq_order_photo_position   UNIQUE (order_id, sort_order)
);

CREATE TABLE order_acceptance (
    id                 uuid                   PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id           uuid                   NOT NULL REFERENCES pickup_order (id),
    recycler_id        uuid                   NOT NULL REFERENCES app_user (id),
    status             acceptance_status_type NOT NULL DEFAULT 'ACCEPTED',
    accepted_at        timestamptz            NOT NULL DEFAULT now(),
    status_changed_at  timestamptz            NOT NULL DEFAULT now(),
    CONSTRAINT uq_order_acceptance_recycler UNIQUE (order_id, recycler_id)     -- RN09, RNF-05
);

-- RN12, RNF-05: un solo reciclador elegido por pedido.
CREATE UNIQUE INDEX uq_order_acceptance_chosen ON order_acceptance (order_id) WHERE status = 'CHOSEN';
-- "Mis pedidos aceptados" (HU-14).
CREATE INDEX ix_order_acceptance_recycler ON order_acceptance (recycler_id, status, accepted_at DESC);

CREATE TABLE order_status_history (
    id           uuid              PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id     uuid              NOT NULL REFERENCES pickup_order (id),
    from_status  order_status_type,                          -- nulo al crear el pedido
    to_status    order_status_type NOT NULL,
    changed_by   uuid              NOT NULL REFERENCES app_user (id),
    reason       varchar(200),
    changed_at   timestamptz       NOT NULL DEFAULT now()
);

CREATE INDEX ix_order_status_history_order ON order_status_history (order_id, changed_at);

-- -----------------------------------------------------------------------------
-- 5. Funciones y triggers
-- -----------------------------------------------------------------------------

-- updated_at automático
CREATE FUNCTION set_updated_at() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END $$;

CREATE TRIGGER trg_app_user_updated_at      BEFORE UPDATE ON app_user      FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_user_profile_updated_at  BEFORE UPDATE ON user_profile  FOR EACH ROW EXECUTE FUNCTION set_updated_at();
CREATE TRIGGER trg_pickup_order_updated_at  BEFORE UPDATE ON pickup_order  FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- RN02 y RN03: el rol y el correo no cambian.
CREATE FUNCTION app_user_protect_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.role <> OLD.role OR NEW.email <> OLD.email THEN
        RAISE EXCEPTION 'EMAIL_AND_ROLE_ARE_IMMUTABLE' USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_app_user_immutable BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION app_user_protect_immutable();

-- RN18: la ubicación es una instantánea; solo cambia la marca de vigencia.
CREATE FUNCTION location_protect_immutable() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NEW.user_id        <> OLD.user_id
       OR NEW.latitude    <> OLD.latitude
       OR NEW.longitude   <> OLD.longitude
       OR NEW.address        IS DISTINCT FROM OLD.address
       OR NEW.district       IS DISTINCT FROM OLD.district
       OR NEW.reference_note IS DISTINCT FROM OLD.reference_note THEN
        RAISE EXCEPTION 'LOCATION_IS_IMMUTABLE' USING ERRCODE = 'integrity_constraint_violation';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_location_immutable BEFORE UPDATE ON location
    FOR EACH ROW EXECUTE FUNCTION location_protect_immutable();

-- RN05: solo un vecino publica pedidos.
CREATE FUNCTION pickup_order_check_neighbor() RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_user WHERE id = NEW.neighbor_id AND role = 'NEIGHBOR') THEN
        RAISE EXCEPTION 'FORBIDDEN_ROLE' USING ERRCODE = 'insufficient_privilege';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_pickup_order_neighbor BEFORE INSERT ON pickup_order
    FOR EACH ROW EXECUTE FUNCTION pickup_order_check_neighbor();

-- RN06: entre 1 y 10 ítems (el mínimo de 1 lo valida el servicio al publicar).
CREATE FUNCTION order_item_check_limit() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    item_count integer;
BEGIN
    PERFORM 1 FROM pickup_order WHERE id = NEW.order_id FOR UPDATE;   -- serializa inserciones concurrentes
    SELECT count(*) INTO item_count FROM order_item WHERE order_id = NEW.order_id;
    IF item_count >= 10 THEN
        RAISE EXCEPTION 'ORDER_ITEM_LIMIT_REACHED' USING ERRCODE = 'check_violation';
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_order_item_limit BEFORE INSERT ON order_item
    FOR EACH ROW EXECUTE FUNCTION order_item_check_limit();

-- RN05 y RN09: solo un reciclador acepta, y solo mientras el pedido está Pendiente.
CREATE FUNCTION order_acceptance_check_insert() RETURNS trigger LANGUAGE plpgsql AS $$
DECLARE
    current_status order_status_type;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM app_user WHERE id = NEW.recycler_id AND role = 'RECYCLER') THEN
        RAISE EXCEPTION 'FORBIDDEN_ROLE' USING ERRCODE = 'insufficient_privilege';
    END IF;

    SELECT status INTO current_status FROM pickup_order WHERE id = NEW.order_id FOR UPDATE;
    IF current_status IS DISTINCT FROM 'PENDING' THEN
        RAISE EXCEPTION 'ORDER_NOT_AVAILABLE' USING ERRCODE = 'serialization_failure';   -- la API responde 409
    END IF;
    RETURN NEW;
END $$;

CREATE TRIGGER trg_order_acceptance_insert BEFORE INSERT ON order_acceptance
    FOR EACH ROW EXECUTE FUNCTION order_acceptance_check_insert();

-- -----------------------------------------------------------------------------
-- 6. Operación atómica: elegir a un reciclador (HU-13, RN12)
-- -----------------------------------------------------------------------------

CREATE FUNCTION confirm_pickup_order(p_order_id uuid, p_acceptance_id uuid, p_neighbor_id uuid)
RETURNS void LANGUAGE plpgsql AS $$
DECLARE
    v_status order_status_type;
BEGIN
    SELECT status INTO v_status
    FROM pickup_order
    WHERE id = p_order_id AND neighbor_id = p_neighbor_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'NOT_ORDER_OWNER' USING ERRCODE = 'insufficient_privilege';
    END IF;
    IF v_status <> 'PENDING' THEN
        RAISE EXCEPTION 'ORDER_NOT_PENDING' USING ERRCODE = 'serialization_failure';   -- 409
    END IF;
    IF NOT EXISTS (SELECT 1 FROM order_acceptance
                   WHERE id = p_acceptance_id AND order_id = p_order_id AND status = 'ACCEPTED') THEN
        RAISE EXCEPTION 'NOT_FOUND' USING ERRCODE = 'no_data_found';
    END IF;

    -- Primero se descartan las demás, luego se elige la indicada.
    UPDATE order_acceptance
       SET status = 'DISCARDED', status_changed_at = now()
     WHERE order_id = p_order_id AND id <> p_acceptance_id AND status = 'ACCEPTED';

    UPDATE order_acceptance
       SET status = 'CHOSEN', status_changed_at = now()
     WHERE id = p_acceptance_id;

    UPDATE pickup_order
       SET status = 'CONFIRMED', confirmed_at = now()
     WHERE id = p_order_id;

    INSERT INTO order_status_history (order_id, from_status, to_status, changed_by)
    VALUES (p_order_id, 'PENDING', 'CONFIRMED', p_neighbor_id);
END $$;

-- -----------------------------------------------------------------------------
-- 7. Distancia aproximada (HU-10): Haversine, sin PostGIS
-- -----------------------------------------------------------------------------

CREATE FUNCTION haversine_km(lat1 double precision, lng1 double precision,
                             lat2 double precision, lng2 double precision)
RETURNS double precision LANGUAGE sql IMMUTABLE PARALLEL SAFE AS $$
    SELECT 2 * 6371.0 * asin(sqrt(
        power(sin(radians(lat2 - lat1) / 2), 2) +
        cos(radians(lat1)) * cos(radians(lat2)) * power(sin(radians(lng2 - lng1) / 2), 2)
    ))
$$;
