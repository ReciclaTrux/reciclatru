/// Contiene soporte de red de network exception para peticiones, autenticación y errores de la API.
import 'package:dio/dio.dart';

/// Categorías de error que las vistas manejan sin depender de Dio.
enum NetworkErrorKind {
  noConnection,
  timeout,
  unauthorized,
  forbidden,
  notFound,
  conflict,
  server,
  unknown,
  serverError,
}

/// Error de red/API con mensaje localizado, estado HTTP y código del backend.
class NetworkException implements Exception {
  const NetworkException({
    required this.kind,
    required this.message,
    this.statusCode,
    this.code,
  });

  final NetworkErrorKind kind;
  final String message;
  final int? statusCode;
  final String? code;

  @override
  String toString() => message;
}

/// Alias conservado para los usos de salud que ya tenían este nombre en Dart.
typedef ApiException = NetworkException;
typedef ApiFailureKind = NetworkErrorKind;

/// Convierte un error de Dio al error común definido por la app.
NetworkException toNetworkException(Object error) {
  if (error is NetworkException) return error;
  if (error is! DioException) {
    return const NetworkException(
      kind: NetworkErrorKind.unknown,
      message: 'No pudimos completar la operación. Inténtalo de nuevo.',
    );
  }
  final status = error.response?.statusCode;
  final body = error.response?.data;
  final code = body is Map ? body['code']?.toString() : null;
  if (status != null) {
    return switch (status) {
      401 => NetworkException(
          kind: NetworkErrorKind.unauthorized,
          message: 'Tu sesión venció. Vuelve a ingresar.',
          statusCode: status,
          code: code,
        ),
      403 => NetworkException(
          kind: NetworkErrorKind.forbidden,
          message: 'No tienes permiso para hacer esto.',
          statusCode: status,
          code: code,
        ),
      404 => NetworkException(
          kind: NetworkErrorKind.notFound,
          message: 'No encontramos la información que buscabas.',
          statusCode: status,
          code: code,
        ),
      409 => NetworkException(
          kind: NetworkErrorKind.conflict,
          message: 'Ese dato ya está en uso. Revisa e inténtalo de nuevo.',
          statusCode: status,
          code: code,
        ),
      >= 500 => NetworkException(
          kind: NetworkErrorKind.server,
          message: 'Tuvimos un problema en el servidor. Inténtalo de nuevo en un momento.',
          statusCode: status,
          code: code,
        ),
      _ => NetworkException(
          kind: NetworkErrorKind.unknown,
          message: 'No pudimos completar la operación. Inténtalo de nuevo.',
          statusCode: status,
          code: code,
        ),
    };
  }
  final timeout = error.type == DioExceptionType.connectionTimeout ||
      error.type == DioExceptionType.receiveTimeout ||
      error.type == DioExceptionType.sendTimeout ||
      error.type == DioExceptionType.transformTimeout;
  if (timeout) {
    return const NetworkException(
      kind: NetworkErrorKind.timeout,
      message: 'La conexión tardó demasiado. Inténtalo de nuevo.',
    );
  }
  if (error.type == DioExceptionType.connectionError) {
    return const NetworkException(
      kind: NetworkErrorKind.noConnection,
      message: 'No tienes conexión. Revisa tu red e inténtalo de nuevo.',
    );
  }
  return const NetworkException(
    kind: NetworkErrorKind.unknown,
    message: 'No pudimos completar la operación. Inténtalo de nuevo.',
  );
}

/// Mensaje localizado para presentar errores de la API al usuario.
String toUserMessage(Object error, {String fallback = 'No pudimos completar la operación. Inténtalo de nuevo.'}) {
  if (error is NetworkException) return error.message;
  if (error is DioException) return toNetworkException(error).message;
  if (error is FormatException && error.message.isNotEmpty) return error.message;
  return fallback;
}

/// Ejecuta una petición y conserva la cancelación como cancelación, no como error.
Future<T> apiCall<T>(Future<T> Function() request) async {
  try {
    return await request();
  } on DioException catch (error) {
    if (error.type == DioExceptionType.cancel) rethrow;
    throw toNetworkException(error);
  }
}
