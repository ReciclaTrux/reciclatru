import 'package:dio/dio.dart';

import '../domain/auth_response.dart';

class AuthRepository {
  final Dio _dio;

  AuthRepository(this._dio);

  // HU-01: Registrar usuario con rol NEIGHBOR o RECYCLER
  Future<void> register(
    String email,
    String password,
    String role,
  ) async {
    try {
      await _dio.post(
        '/party-data-management/initiation',
        data: {
          'email': email.trim().toLowerCase(),
          'password': password,
          'role': role,
        },
      );
      // Como en Kotlin, HU-01 no depende de los campos opcionales del
      // registro retornado por el backend.
    } on DioException catch (e) {
      if (e.response?.statusCode == 409) {
        throw Exception('Ya existe una cuenta con este correo');
      }
      if (e.response?.statusCode == 429) {
        throw Exception('Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.');
      }
      throw Exception(_extractErrorMessage(e));
    }
  }

  // HU-02: Iniciar sesión (Devuelve el accessToken)
  Future<AuthResponse> login(
    String email,
    String password,
  ) async {
    try {
      final response = await _dio.post(
        '/party-authentication/initiation',
        data: {
          'email': email.trim().toLowerCase(),
          'password': password,
        },
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        return AuthResponse.fromJson(
          Map<String, dynamic>.from(response.data),
        );
      }

      throw Exception('Error al iniciar sesión');
    } on DioException catch (e) {
      throw Exception(_extractErrorMessage(e));
    }
  }

  // Obtener mensaje de error amigable
  String _extractErrorMessage(DioException e) {
    final data = e.response?.data;

    if (data is Map) {
      return data['detail']?.toString() ??
          data['message']?.toString() ??
          'Error desconocido en el servidor';
    }

    if (e.type == DioExceptionType.connectionTimeout) {
      return 'Tiempo de espera agotado al conectar con el servidor.';
    }

    if (e.type == DioExceptionType.receiveTimeout) {
      return 'El servidor tardó demasiado en responder.';
    }

    if (e.type == DioExceptionType.connectionError) {
      return 'No se pudo conectar con el servidor. Verifica tu red.';
    }

    return 'Error de conexión con el servidor.';
  }
}
