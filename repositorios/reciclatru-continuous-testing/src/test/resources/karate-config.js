// Se ejecuta antes de cada escenario. Todo lo que devuelve queda como variable global en los features.
// Cambiar de entorno:  -Dkarate.env=test   o apuntar directo:  -DbaseUrl=https://mi-api/api/v1
function fn() {
  var env = karate.env || 'local';
  var urls = { local: 'http://localhost:3000/api/v1' };

  // En CI no se asume ningún servidor: hay que decir cuál (y debe ser de pruebas).
  if (env === 'ci' && !karate.properties['baseUrl']) {
    karate.fail('Falta -DbaseUrl con la URL del API de pruebas (termina en /api/v1).');
  }

  var config = {
    env: env,
    baseUrl: karate.properties['baseUrl'] || urls[env] || urls.local,

    // Contraseña de los usuarios de prueba (cumple el mínimo de 8 caracteres).
    testPassword: 'Reciclatru2026',

    // Punto de referencia: Huanchaco, Trujillo.
    lat: -8.077806,
    lng: -79.122183,
    // Un punto lejano (centro de Trujillo) para probar el orden por distancia.
    farLat: -8.111600,
    farLng: -79.028800,

    // Origen que la API tiene permitido por CORS (variable CORS_ORIGINS del API).
    corsAllowedOrigin: karate.properties['corsOrigin'] || 'http://localhost:5173',

    UUID_ZERO: '00000000-0000-4000-8000-000000000000'
  };

  karate.configure('connectTimeout', 15000);
  karate.configure('readTimeout', 60000); // Render gratuito puede tardar en despertar
  // Cada petición lleva un x-correlation-id reconocible: sirve para buscarla en los logs (Grafana).
  karate.configure('headers', read('classpath:headers.js'));

  /** Cabecera Authorization de un usuario creado con create-user.feature. */
  config.bearer = function (token) {
    return 'Bearer ' + token;
  };

  /** Forma mínima de un error RFC 7807 (application/problem+json) con su código de negocio. */
  config.problem = function (status, code) {
    return {
      type: '#string',
      title: '#string',
      status: status,
      code: code,
      instance: '#string',
      correlationId: '#string'
    };
  };

  /** ¿La respuesta trae esa cabecera? (sin distinguir mayúsculas) */
  config.hasHeader = function (headers, name) {
    var keys = karate.keysOf(headers);
    for (var i = 0; i < keys.length; i++) {
      if (String(keys[i]).toLowerCase() === name.toLowerCase()) return true;
    }
    return false;
  };

  /** true si la lista está ordenada de menor a mayor. */
  config.isAscending = function (list) {
    for (var i = 1; i < list.length; i++) {
      if (list[i] < list[i - 1]) return false;
    }
    return true;
  };

  return config;
}
