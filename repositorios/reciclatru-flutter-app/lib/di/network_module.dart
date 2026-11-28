/// Registra las dependencias de network module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:reciclatru/data/remote/api_urls.dart';
import 'package:reciclatru/data/remote/auth_interceptor.dart';
import 'session_module.dart';

/// Respeta el mismo comportamiento de AuthInterceptor.kt.
final dioProvider = Provider<Dio>((ref) {
  final sessionStore = ref.read(sessionStoreProvider);
  final dio = Dio(BaseOptions(
    baseUrl: apiBaseUrl,
    connectTimeout: const Duration(seconds: 10),
    receiveTimeout: const Duration(seconds: 60),
    headers: const {'Accept': 'application/json'},
  ));
  dio.interceptors.add(AuthInterceptor(sessionStore));
  ref.onDispose(() => dio.close());
  return dio;
});
