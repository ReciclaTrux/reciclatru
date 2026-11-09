import 'package:dio/dio.dart';
import '../../../core/session/session_store.dart';
import '../domain/login_result.dart';

class AuthRepository {
  AuthRepository(this._dio, this._store);
  final Dio _dio;
  final SessionStore _store;

  // HU-01. Mantiene la misma firma de Flutter integrada previamente.
  Future<void> register(String email, String password, String role) async {
    try {
      await _dio.post('/party-data-management/initiation', data: {
        'email': email.trim().toLowerCase(),
        'password': password,
        'role': role,
      });
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

  /// Igual que AuthMapper.kt: exige token, identificador y rol válidos.
  Future<LoginResult> login(String email, String password) async {
    try {
      final response = await _dio.post('/party-authentication/initiation', data: {
        'email': email.trim(),
        'password': password,
      });
      final data = Map<String, dynamic>.from(response.data as Map);
      final record = data['partyDataManagementInstanceRecord'] is Map
          ? Map<String, dynamic>.from(data['partyDataManagementInstanceRecord'] as Map)
          : <String, dynamic>{};
      final token = data['accessToken'] as String?;
      final userId = (data['crReferenceId'] ?? record['id']) as String?;
      final role = record['role'] as String?;
      if (token == null || token.isEmpty || userId == null || userId.isEmpty ||
          (role != 'NEIGHBOR' && role != 'RECYCLER')) {
        throw Exception('No pudimos completar la operación. Inténtalo de nuevo.');
      }
      final session = AppSession(userId: userId, token: token, role: role!);
      await _store.save(session, needsProfile: record['profileComplete'] != true);
      return LoginResult(session: session, profileComplete: record['profileComplete'] == true);
    } on DioException catch (e) {
      if (e.response?.statusCode == 400 || e.response?.statusCode == 401) {
        throw Exception('Correo o contraseña incorrectos.');
      }
      if (e.response?.statusCode == 429) {
        throw Exception('Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.');
      }
      throw Exception(_extractErrorMessage(e));
    }
  }

  Future<void> logout() => _store.clear(SignOutReason.logout);

  String _extractErrorMessage(DioException e) {
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.sendTimeout) {
      return 'Tiempo de espera agotado al conectar con el servidor.';
    }
    if (e.type == DioExceptionType.receiveTimeout) {
      return 'El servidor tardó demasiado en responder.';
    }
    if (e.type == DioExceptionType.connectionError) {
      return 'No se pudo conectar con el servidor. Verifica tu red.';
    }
    final data = e.response?.data;
    if (data is Map) {
      return (data['detail'] ?? data['message'])?.toString() ??
          'No pudimos completar la operación. Inténtalo de nuevo.';
    }
    return 'No pudimos completar la operación. Inténtalo de nuevo.';
  }
}
