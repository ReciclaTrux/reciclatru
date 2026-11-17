// Configuración común. Todo se puede cambiar con variables: k6 run -e BASE_URL=... -e VUS=50 ...

export const BASE_URL = (__ENV.BASE_URL || 'http://localhost:3000').replace(/\/+$/, '');
export const API_PREFIX = __ENV.API_PREFIX || '/api/v1';
export const API = `${BASE_URL}${API_PREFIX}`;

// Usuarios de prueba que se crean al iniciar (setup). Los VUs los reutilizan.
export const USERS_PER_ROLE = Number(__ENV.USERS || 10);
export const PASSWORD = __ENV.TEST_PASSWORD || 'Reciclatru2026';

// Usuarios virtuales en el punto más alto de la prueba.
export const PEAK_VUS = Number(__ENV.VUS || 20);

// Multiplicador del tiempo de espera entre acciones (0 = sin pausas, máxima presión).
export const THINK = Number(__ENV.THINK || 1);

// Fracción de iteraciones que hacen un login real (cuesta CPU por bcrypt).
export const LOGIN_RATE = Number(__ENV.LOGIN_RATE || 0.05);

// Punto de referencia para ordenar pedidos por distancia (Huanchaco, Trujillo).
export const LAT = -8.077806;
export const LNG = -79.122183;

// Metas. Las medidas aplican solo a las peticiones de la prueba (phase:run), no a la preparación.
// Requisito del sistema: TODA petición responde en un máximo de 2 s. Se exige sobre el máximo observado.
// Para explorar un entorno lento sin que la prueba "falle", sube el máximo: -e MAX_MS=5000.
const MAX = Number(__ENV.MAX_MS || 2000);
const P95 = Number(__ENV.P95_MS || MAX);
const ERR = Number(__ENV.MAX_ERROR_RATE || 0.01);

export const THRESHOLDS = {
  'http_req_failed{phase:run}': [`rate<${ERR}`],
  'http_req_duration{phase:run}': [`max<${MAX}`, `p(95)<${P95}`],
  // El login incluye bcrypt, pero también debe cumplir el máximo.
  'http_req_duration{name:login}': [`max<${Number(__ENV.LOGIN_MAX_MS || MAX)}`],
  checks: ['rate>0.99'],
};