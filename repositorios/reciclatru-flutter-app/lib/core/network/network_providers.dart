import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'api_config.dart';
import '../session/session_store.dart';

final sessionStoreProvider = Provider<SessionStore>((ref) {
  final store = SessionStore();
  ref.onDispose(store.dispose);
  return store;
});

/// Respeta el mismo comportamiento de AuthInterceptor.kt.
final dioProvider = Provider<Dio>((ref) {
  final sessionStore = ref.read(sessionStoreProvider);
  final dio = Dio(BaseOptions(
    baseUrl: apiBaseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 60),
    headers: const {'Accept': 'application/json'},
  ));
  dio.interceptors.add(InterceptorsWrapper(
    onRequest: (options, handler) {
      final token = sessionStore.currentSession?.token;
      if (token != null && token.isNotEmpty) {
        options.headers['Authorization'] = 'Bearer $token';
      }
      handler.next(options);
    },
    onError: (error, handler) async {
      // Igual que Kotlin: solo expirar si el 401 corresponde a un request
      // autenticado, evitando invalidar el login por credenciales erróneas.
      final authorization = error.requestOptions.headers['Authorization'];
      if (error.response?.statusCode == 401 && authorization != null) {
        await sessionStore.clear(SignOutReason.expired);
      }
      handler.next(error);
    },
  ));
  ref.onDispose(() => dio.close());
  return dio;
});
