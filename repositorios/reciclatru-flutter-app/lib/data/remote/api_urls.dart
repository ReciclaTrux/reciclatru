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

/// Convierte rutas de imágenes de la API en direcciones que Image.network puede abrir.
///
/// Kotlin aplica la misma regla: conserva las URLs completas y añade el dominio
/// de la API cuando el servidor responde solo con una ruta como `/uploads/foto.jpg`.
String toAbsoluteApiUrl(String value) {
  final url = value.trim();
  if (url.isEmpty || url.startsWith('http://') || url.startsWith('https://')) {
    return url;
  }

  final origin = Uri.parse(apiBaseUrl).origin;
  return '$origin${url.startsWith('/') ? url : '/$url'}';
}

/// Normaliza las fotos de un pedido antes de convertir el JSON en un modelo.
///
/// La API puede devolver una miniatura, una foto o varias fotos. Si una ruta no
/// trae el dominio, se añade para que las tarjetas y galerías puedan cargarla.
Map<String, dynamic> withAbsoluteApiImageUrls(Map<String, dynamic> json) {
  final photos = json['photos'];
  return {
    ...json,
    if (json['thumbnailUrl'] != null)
      'thumbnailUrl': toAbsoluteApiUrl(json['thumbnailUrl'].toString()),
    if (json['url'] != null) 'url': toAbsoluteApiUrl(json['url'].toString()),
    if (photos is List)
      'photos': photos.whereType<Map>().map((photo) {
        final item = Map<String, dynamic>.from(photo);
        final url = item['url'];
        if (url != null) item['url'] = toAbsoluteApiUrl(url.toString());
        return item;
      }).toList(),
  };
}
