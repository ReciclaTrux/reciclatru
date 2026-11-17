import { check, fail, sleep } from 'k6';
import { call, json } from './http.js';
import { PASSWORD, USERS_PER_ROLE, LAT, LNG } from './config.js';

const SETUP = { phase: 'setup' };

// Render gratuito se duerme: la primera petición puede tardar hasta ~1 minuto.
function waitForApi() {
  for (let i = 0; i < 30; i++) {
    const res = call('GET', '/health', { ...SETUP, name: 'health_wakeup' });
    if (res.status === 200) return;
    sleep(2);
  }
  fail('El API no respondió /health en ~60 s. ¿BASE_URL correcta y API encendida?');
}

function must(res, expected, what) {
  if (res.status === 429) {
    fail(`${what}: 429. Desactiva el límite de solicitudes en el entorno de pruebas (RATE_LIMIT_ENABLED=false).`);
  }
  if (res.status !== expected) {
    fail(`${what}: se esperaba ${expected} y llegó ${res.status}. ${res.body}`);
  }
}

function createUser(runId, role, i) {
  const prefix = role === 'NEIGHBOR' ? 'n' : 'r';
  const email = `perf.${runId}.${prefix}${i}@example.com`;
  const phone = `9${String(10000000 + Math.floor(Math.random() * 89999999))}`;

  must(call('POST', '/party-data-management/initiation', { ...SETUP, name: 'setup_register', body: { email, password: PASSWORD, role } }), 201, 'registro');

  const login = call('POST', '/party-authentication/initiation', { ...SETUP, name: 'setup_login', body: { email, password: PASSWORD } });
  must(login, 200, 'login');
  const { accessToken, crReferenceId } = json(login);

  const profile = role === 'NEIGHBOR'
    ? { fullName: `Vecino Prueba ${i}`, phone, reference: 'Prueba de carga' }
    : { fullName: `Reciclador Prueba ${i}`, phone };
  must(call('PUT', `/party-data-management/${crReferenceId}/update`, { ...SETUP, name: 'setup_profile', token: accessToken, body: profile }), 200, 'perfil');

  if (role === 'NEIGHBOR') {
    must(call('PUT', '/location-data-management/me/pickup-point/update', {
      ...SETUP, name: 'setup_location', token: accessToken,
      body: { latitude: LAT, longitude: LNG, address: 'Av. Prueba 123, Huanchaco', district: 'Huanchaco' },
    }), 200, 'ubicación');
    // Deja pedidos PENDING para que los recicladores tengan qué listar y aceptar.
    for (let k = 0; k < 3; k++) {
      must(call('POST', '/servicing-order/initiation', {
        ...SETUP, name: 'setup_order', token: accessToken,
        body: { items: [{ description: 'Botellas de plástico PET', weightKg: 3.5 }] },
      }), 201, 'pedido inicial');
    }
  }
  return { email, token: accessToken, id: crReferenceId };
}

// Crea los usuarios de la corrida. Los correos llevan un id único, así se puede repetir sin choques.
export function prepare() {
  waitForApi();
  const runId = Date.now();
  const neighbors = [];
  const recyclers = [];
  for (let i = 0; i < USERS_PER_ROLE; i++) neighbors.push(createUser(runId, 'NEIGHBOR', i));
  for (let i = 0; i < USERS_PER_ROLE; i++) recyclers.push(createUser(runId, 'RECYCLER', i));
  check(neighbors, { 'usuarios de prueba creados': (u) => u.length === USERS_PER_ROLE });
  return { neighbors, recyclers };
}
