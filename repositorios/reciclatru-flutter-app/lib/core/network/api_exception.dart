/// Motivo por el que falló una llamada a la API.
enum ApiFailureKind { noConnection, serverError }

/// Error común de red: la app solo maneja este tipo, nunca `DioException`.
class ApiException implements Exception {
  const ApiException({
    required this.kind,
    required this.message,
    this.statusCode,
  });

  final ApiFailureKind kind;
  final String message;

  /// Código HTTP cuando la API llegó a responder.
  final int? statusCode;

  @override
  String toString() => 'ApiException($kind, $statusCode, $message)';
}
