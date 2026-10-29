-- =============================================================================
-- Celular ofuscado y referencia del vecino en el perfil.
--
-- user_profile.phone          : celular ofuscado en claro (987****21). Solo sirve para mostrar, auditar y depurar.
-- user_profile.phone_encrypted: celular real cifrado por la API (AES-256-GCM). La API lo descifra solo para
--                               mostrárselo a la contraparte autorizada (HU-12 y HU-13).
-- user_profile.reference_note : referencia del vecino (HU-03). Se lee en vivo, como el celular; la ubicación
--                               (latitud, longitud, dirección, distrito) sigue siendo una instantánea inmutable (RN18).
-- order_item.position       : conserva el orden de los ítems (todos comparten created_at dentro de la transacción).
-- Los perfiles que ya existían quedan con el celular ofuscado y sin cifrado: la API los trata como perfil
-- incompleto hasta que la persona vuelva a guardar su celular.
-- =============================================================================

ALTER TABLE user_profile DROP CONSTRAINT ck_user_profile_phone;

ALTER TABLE user_profile ALTER COLUMN phone TYPE varchar(9);

UPDATE user_profile
   SET phone = left(phone, 3) || '****' || right(phone, 2)
 WHERE phone ~ '^9[0-9]{8}$';

ALTER TABLE user_profile
    ADD CONSTRAINT ck_user_profile_phone CHECK (phone ~ '^9[0-9]{2}\*{4}[0-9]{2}$');   -- RN04, ofuscado

ALTER TABLE user_profile ADD COLUMN phone_encrypted text;
ALTER TABLE user_profile ADD COLUMN reference_note varchar(150);

-- Posición de cada ítem dentro del pedido (el orden en que los escribió el vecino).
ALTER TABLE order_item ADD COLUMN position smallint NOT NULL DEFAULT 0;

COMMENT ON COLUMN user_profile.phone           IS 'Celular ofuscado (987****21); el valor real está en phone_encrypted';
COMMENT ON COLUMN user_profile.phone_encrypted IS 'Celular real cifrado por la API (AES-256-GCM, formato iv.tag.datos en base64)';
COMMENT ON COLUMN user_profile.reference_note  IS 'Referencia del vecino para ubicar la puerta; se lee en vivo';
COMMENT ON COLUMN order_item.position         IS 'Orden del ítem dentro del pedido, empezando en 1';
COMMENT ON COLUMN location.reference_note      IS 'Sin uso desde V20261027_1100: la referencia vive en user_profile.reference_note';
