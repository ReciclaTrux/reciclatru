import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'api_config.dart';

/// Cliente HTTP compartido por todas las features.
///
/// El `receiveTimeout` es largo a propósito: un servicio en Render (plan
/// gratuito) puede tardar cerca de un minuto en despertar.
final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(
    BaseOptions(
      baseUrl: apiBaseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 60),
      headers: const <String, String>{'Accept': 'application/json'},
    ),
  );
  ref.onDispose(dio.close);
  return dio;
});
