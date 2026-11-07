import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart'; // Importa el dioProvider global que ya usa apiBaseUrl
import '../domain/auth_response.dart';
import '../domain/user_model.dart';

class AuthRepository {
  final Dio _dio;

  AuthRepository(this._dio);

  // HU-01: Registrar usuario con rol NEIGHBOR o RECYCLER
  Future<UserModel> register(
    String email,
    String password,
    String role,
  ) async {
    try {
      final response = await _dio.post(
        '/party-data-management/initiation',//
        data: {
          'email': email.trim().toLowerCase(),
          'password': password,
          'role': role, // "NEIGHBOR" o "RECYCLER"
        },
      );

      if (response.statusCode == 200 || response.statusCode == 201) {
        final responseData = response.data;

        final data = responseData is Map<String, dynamic>
            ? responseData['partyDataManagementInstanceRecord'] ?? responseData
            : responseData;

        return UserModel.fromJson(
          Map<String, dynamic>.from(data),
        );
      }

      throw Exception('Error al registrar usuario');
    } on DioException catch (e) {
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

// Proveedor para conectar el repositorio con la UI mediante Riverpod
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final dio = ref.watch(dioProvider); // Toma automáticamente el Dio configurado con apiBaseUrl
  return AuthRepository(dio);
});