/// Implementa el repositorio auth repository impl conectando los contratos del dominio con sus fuentes de datos.
import 'package:dio/dio.dart';
import '../../remote/api/auth/auth_api.dart';
import '../../remote/dto/auth/auth_dtos.dart';
import '../../remote/mapper/auth/auth_mapper.dart';
import '../../remote/network_exception.dart';
import 'package:reciclatru/data/local/session/session_store.dart';
import 'package:reciclatru/model/auth/login_result.dart';
import 'package:reciclatru/model/session/session.dart';
import 'package:reciclatru/model/session/sign_out_reason.dart';
import 'package:reciclatru/domain/repository/auth/auth_repository.dart' as contract;

class AuthRepositoryImpl implements contract.AuthRepository {
  AuthRepositoryImpl(Dio dio, this._store) : _api = AuthApi(dio);
  final SessionStore _store;
  final AuthApi _api;

  // HU-01. Mantiene la misma firma de Flutter integrada previamente.
  Future<void> register(String email, String password, String role) async {
    try {
      await _api.register(RegisterRequestDto(
        email: email.trim().toLowerCase(),
        password: password,
        role: role,
      ));
    } on DioException catch (e) {
      throw _toAuthException(e);
    }
  }

  /// Igual que AuthMapper.kt: exige token, identificador y rol válidos.
  Future<LoginResult> login(String email, String password) async {
    try {
      final dto = await _api.login(LoginRequestDto(email: email.trim(), password: password));
      final result = toLoginResult(dto);
      await _store.save(result.session, needsProfile: !result.profileComplete);
      return result;
    } on DioException catch (e) {
      throw _toAuthException(e, login: true);
    }
  }

  Future<void> logout() => _store.clear(SignOutReason.logout);

  NetworkException _toAuthException(DioException e, {bool login = false}) {
    final status = e.response?.statusCode;
    if (login && (status == 400 || status == 401)) {
      return NetworkException(
        kind: NetworkErrorKind.unauthorized,
        message: 'Correo o contraseña incorrectos.',
        statusCode: status,
      );
    }
    if (!login && status == 409) {
      return const NetworkException(
        kind: NetworkErrorKind.conflict,
        message: 'Ya existe una cuenta con este correo',
        statusCode: 409,
      );
    }
    if (status == 429) {
      return NetworkException(
        kind: NetworkErrorKind.unknown,
        message: 'Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.',
        statusCode: status,
      );
    }
    if (e.type == DioExceptionType.connectionTimeout ||
        e.type == DioExceptionType.sendTimeout) {
      return const NetworkException(kind: NetworkErrorKind.timeout, message: 'Tiempo de espera agotado al conectar con el servidor.');
    }
    if (e.type == DioExceptionType.receiveTimeout) {
      return const NetworkException(kind: NetworkErrorKind.timeout, message: 'El servidor tardó demasiado en responder.');
    }
    if (e.type == DioExceptionType.connectionError) {
      return const NetworkException(kind: NetworkErrorKind.noConnection, message: 'No se pudo conectar con el servidor. Verifica tu red.');
    }
    final data = e.response?.data;
    if (data is Map) {
      return NetworkException(
        kind: NetworkErrorKind.unknown,
        message: (data['detail'] ?? data['message'])?.toString() ??
            'No pudimos completar la operación. Inténtalo de nuevo.',
        statusCode: status,
      );
    }
    return NetworkException(
      kind: NetworkErrorKind.unknown,
      message: 'No pudimos completar la operación. Inténtalo de nuevo.',
      statusCode: status,
    );
  }
}
