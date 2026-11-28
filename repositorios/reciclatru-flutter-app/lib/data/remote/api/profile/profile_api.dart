/// Declara las llamadas HTTP de profile api con los mismos endpoints que consume la app Kotlin.
import 'package:dio/dio.dart';

import '../../dto/profile/party_profile_dtos.dart';
import '../../dto/profile/pickup_point_dtos.dart';

/// Endpoints de perfil usando el usuario de la sesión actual (`me`).
class ProfileApi {
  const ProfileApi(this._dio);
  final Dio _dio;

  Future<PartyProfileOutputDto> getProfile() async {
    final response = await _dio.get('/party-data-management/me/retrieval');
    return PartyProfileOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }

  Future<PartyProfileOutputDto> updateProfile(ProfileUpdateRequestDto body) async {
    final response = await _dio.put('/party-data-management/me/update', data: body.toJson());
    return PartyProfileOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }

  Future<PartyProfileOutputDto> updatePickupPoint(PickupPointRequestDto body) async {
    final response = await _dio.put('/location-data-management/me/pickup-point/update', data: body.toJson());
    return PartyProfileOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }
}
