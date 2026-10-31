/// URL base de la API de ReciclaTru.
///
/// Se define al compilar, sin tocar el código:
/// `flutter run --dart-define=API_BASE_URL=https://mi-api.onrender.com`
///
/// Valores típicos: `http://10.0.2.2:3000` (emulador Android contra la API en
/// tu PC), `http://localhost:3000` (Edge/Windows) o la URL desplegada.
const String apiBaseUrl = String.fromEnvironment(
  'API_BASE_URL',
  defaultValue: 'https://reciclatru.onrender.com/api/v1',
);

/// Ruta del endpoint de salud de la API.
const String apiHealthPath = String.fromEnvironment(
  'API_HEALTH_PATH',
  defaultValue: '/health',
);
