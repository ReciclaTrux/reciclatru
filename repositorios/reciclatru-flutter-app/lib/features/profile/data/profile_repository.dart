import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../domain/user_profile.dart';

/// Equivalente de ProfileApi + UserProfileRepositoryImpl en Kotlin.
class ProfileRepository {
  const ProfileRepository(this._dio);
  final Dio _dio;

  Future<UserProfile> getProfile() async {
    try {
      final result = await _dio.get('/party-data-management/me/retrieval');
      return UserProfile.fromJson(Map<String, dynamic>.from(result.data as Map));
    } catch (error) {
      throw Exception(_message(error));
    }
  }

  Future<UserProfile> updateProfile({
    required String fullName,
    required String phone,
    required String? reference,
  }) async {
    try {
      final result = await _dio.put(
        '/party-data-management/me/update',
        data: {
          'fullName': fullName.trim(),
          'phone': phone.trim(),
          'reference': reference?.trim(),
        },
      );
      return UserProfile.fromJson(Map<String, dynamic>.from(result.data as Map));
    } catch (error) {
      if (error is DioException && error.response?.statusCode == 400) {
        throw Exception('Revisa tus datos e inténtalo de nuevo.');
      }
      throw Exception(_message(error));
    }
  }

  String _message(Object error) {
    if (error is FormatException) return error.message;
    if (error is DioException) {
      if (error.type == DioExceptionType.connectionTimeout ||
          error.type == DioExceptionType.sendTimeout ||
          error.type == DioExceptionType.receiveTimeout) {
        return 'Tiempo de espera agotado al conectar con el servidor.';
      }
      if (error.type == DioExceptionType.connectionError) {
        return 'No se pudo conectar con el servidor. Verifica tu red.';
      }
      final data = error.response?.data;
      if (data is Map) return (data['detail'] ?? data['message'])?.toString() ?? 'No pudimos completar la operación. Inténtalo de nuevo.';
    }
    return 'No pudimos completar la operación. Inténtalo de nuevo.';
  }
}

final profileRepositoryProvider = Provider<ProfileRepository>((ref) {
  return ProfileRepository(ref.watch(dioProvider));
});
