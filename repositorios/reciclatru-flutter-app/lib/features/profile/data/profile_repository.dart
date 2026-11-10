
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/network/network_providers.dart';
import '../domain/user_profile.dart';

class ProfileRepository {
  const ProfileRepository(this._dio);

  final Dio _dio;

  Future<UserProfile> getProfile() async {
    try {
      final response = await _dio.get(
        '/party-data-management/me/retrieval',
      );

      return UserProfile.fromJson(
        Map<String, dynamic>.from(response.data as Map),
      );
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
      final response = await _dio.put(
        '/party-data-management/me/update',
        data: {
          'fullName': fullName.trim(),
          'phone': phone.trim(),
          'reference': reference?.trim(),
        },
      );

      return UserProfile.fromJson(
        Map<String, dynamic>.from(response.data as Map),
      );
    } catch (error) {
      if (error is DioException &&
          error.response?.statusCode == 400) {
        throw Exception('Revisa tus datos e inténtalo de nuevo.');
      }

      throw Exception(_message(error));
    }
  }

  /// HU-04: Guarda el punto de recojo del vecino.
  /// El backend identifica al usuario mediante su token.
  Future<PickupAddress> updatePickupPoint({
    required double latitude,
    required double longitude,
    required String address,
    required String district,
  }) async {
    try {
      final response = await _dio.put(
        '/location-data-management/me/pickup-point/update',
        data: {
          'latitude': latitude,
          'longitude': longitude,
          'address': address.trim(),
          'district': district.trim(),
        },
      );

      final raw = Map<String, dynamic>.from(response.data as Map);
      final record = raw['partyDataManagementInstanceRecord'];

      if (record is! Map) {
        throw const FormatException(
          'El servidor no devolvió el perfil actualizado.',
        );
      }

      final point = record['pickupPoint'];
      if (point is! Map) {
        return PickupAddress(
          latitude: latitude,
          longitude: longitude,
          address: address.trim(),
          district: district.trim(),
        );
      }

      return PickupAddress(
        latitude: point['latitude'] is num
            ? (point['latitude'] as num).toDouble()
            : latitude,
        longitude: point['longitude'] is num
            ? (point['longitude'] as num).toDouble()
            : longitude,
        address: point['address']?.toString().trim().isNotEmpty == true
            ? point['address'].toString()
            : address.trim(),
        district: point['district']?.toString().trim().isNotEmpty == true
            ? point['district'].toString()
            : district.trim(),
      );
    } catch (error) {
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

      if (data is Map) {
        return (data['detail'] ?? data['message'])?.toString() ??
            'No pudimos completar la operación. Inténtalo de nuevo.';
      }
    }

    return 'No pudimos completar la operación. Inténtalo de nuevo.';
  }
}

final profileRepositoryProvider = Provider<ProfileRepository>((ref) {
  return ProfileRepository(ref.watch(dioProvider));
});
