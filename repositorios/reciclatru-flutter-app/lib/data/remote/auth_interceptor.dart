/// Contiene soporte de red de auth interceptor para peticiones, autenticación y errores de la API.
import 'package:dio/dio.dart';

import '../local/session/session_store.dart';
import '../../model/session/sign_out_reason.dart';

/// Agrega el token y expira solo las solicitudes protegidas con respuesta 401.
class AuthInterceptor extends Interceptor {
  AuthInterceptor(this._sessionStore);
  final SessionStore _sessionStore;

  @override
  void onRequest(RequestOptions options, RequestInterceptorHandler handler) {
    final token = _sessionStore.currentSession?.token;
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  @override
  void onError(DioException error, ErrorInterceptorHandler handler) async {
    final sentAuthorization = error.requestOptions.headers['Authorization'];
    if (error.response?.statusCode == 401 && sentAuthorization != null) {
      await _sessionStore.clear(SignOutReason.expired);
    }
    handler.next(error);
  }
}
