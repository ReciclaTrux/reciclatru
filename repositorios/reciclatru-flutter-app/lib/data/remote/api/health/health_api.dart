/// Declara las llamadas HTTP de health api con los mismos endpoints que consume la app Kotlin.
import 'package:dio/dio.dart';

import 'package:reciclatru/data/remote/api_urls.dart';
import 'package:reciclatru/data/remote/network_exception.dart';
import '../../dto/health/health_dto.dart';

/// Resultado de una prueba de salud correcta.
class HealthStatus {
  const HealthStatus({
    required this.statusCode,
    required this.elapsed,
    this.status,
  });

  /// Código HTTP de la respuesta (2xx).
  final int statusCode;

  /// Tiempo que tardó la API en responder.
  final Duration elapsed;

  /// Campo `status` del cuerpo, si la API lo envía (p. ej. `ok`).
  final String? status;
}

/// Llama al endpoint de salud de la API.
class HealthApi {
  HealthApi(this._dio);

  final Dio _dio;

  /// Devuelve [HealthStatus] o lanza [ApiException].
  Future<HealthStatus> check() async {
    final stopwatch = Stopwatch()..start();
    try {
      final response = await _dio.get<dynamic>(apiHealthPath);
      stopwatch.stop();
      final body = response.data;
      final dto = body is Map
          ? HealthDto.fromJson(Map<String, dynamic>.from(body))
          : const HealthDto();
      return HealthStatus(
        statusCode: response.statusCode ?? 200,
        elapsed: stopwatch.elapsed,
        status: dto.status,
      );
    } on DioException catch (error) {
      final api = _toApiException(error);
      // Detalle técnico para diagnosticar (CORS, DNS, certificado, etc.).
      throw ApiException(
        kind: api.kind,
        statusCode: api.statusCode,
        message: '${api.message}\n[${error.type.name}] '
            '${error.error ?? error.message}',
      );
    }
  }
}

ApiException _toApiException(DioException error) {
  switch (error.type) {
    case DioExceptionType.badResponse:
      final code = error.response?.statusCode;
      return ApiException(
        kind: ApiFailureKind.serverError,
        statusCode: code,
        message: 'La API respondió con el código $code. Intenta de nuevo.',
      );
    case DioExceptionType.connectionTimeout:
    case DioExceptionType.sendTimeout:
    case DioExceptionType.receiveTimeout:
    case DioExceptionType.transformTimeout:
      return const ApiException(
        kind: ApiFailureKind.noConnection,
        message: 'La API tardó demasiado en responder. Intenta de nuevo.',
      );
    case DioExceptionType.badCertificate:
    case DioExceptionType.connectionError:
    case DioExceptionType.cancel:
    case DioExceptionType.unknown:
      return const ApiException(
        kind: ApiFailureKind.noConnection,
        message: 'No pudimos conectar con la API. Revisa tu conexión e '
            'intenta de nuevo.',
      );
  }
}

