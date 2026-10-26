-- Datos de prueba SOLO para desarrollo local. Nunca se ejecuta contra Neon de producción.
-- Uso:
--   docker compose exec -T db psql -U reciclatru -d reciclatru < seeds/dev_seed.sql
--
-- Las contraseñas son un hash bcrypt de ejemplo (no corresponde a una clave real).

INSERT INTO app_user (id, email, password_hash, role) VALUES
    ('11111111-1111-1111-1111-111111111111', 'vecino@correo.com',
     '$2b$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345', 'NEIGHBOR'),
    ('22222222-2222-2222-2222-222222222222', 'reciclador@correo.com',
     '$2b$10$abcdefghijklmnopqrstuuABCDEFGHIJKLMNOPQRSTUVWXYZ012345', 'RECYCLER');

INSERT INTO user_profile (user_id, full_name, phone) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Vecino de Prueba', '987654321'),
    ('22222222-2222-2222-2222-222222222222', 'Reciclador de Prueba', '912345678');

INSERT INTO location (id, user_id, latitude, longitude, address, district, reference_note) VALUES
    ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
     -8.080000, -79.120000, 'Av. Larco 100', 'Huanchaco', 'Puerta negra, frente al parque');

INSERT INTO pickup_order (id, neighbor_id, pickup_location_id) VALUES
    ('44444444-4444-4444-4444-444444444444', '11111111-1111-1111-1111-111111111111',
     '33333333-3333-3333-3333-333333333333');

INSERT INTO order_item (order_id, description, weight_kg) VALUES
    ('44444444-4444-4444-4444-444444444444', 'Botellas de plástico', 2.5),
    ('44444444-4444-4444-4444-444444444444', 'Cartón', 4.0);

INSERT INTO order_status_history (order_id, from_status, to_status, changed_by) VALUES
    ('44444444-4444-4444-4444-444444444444', NULL, 'PENDING', '11111111-1111-1111-1111-111111111111');
