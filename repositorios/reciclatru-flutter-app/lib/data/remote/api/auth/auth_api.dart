/// Declara las llamadas HTTP de auth api con los mismos endpoints que consume la app Kotlin.
import 'package:dio/dio.dart';

import '../../dto/auth/auth_dtos.dart';

/// Endpoints de autenticación equivalentes a los de Retrofit en Kotlin.
class AuthApi {
  const AuthApi(this._dio);
  final Dio _dio;

  Future<PartyDataManagementOutputDto> register(RegisterRequestDto body) async {
    final response = await _dio.post('/party-data-management/initiation', data: body.toJson());
    return PartyDataManagementOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }

  Future<PartyAuthenticationOutputDto> login(LoginRequestDto body) async {
    final response = await _dio.post('/party-authentication/initiation', data: body.toJson());
    return PartyAuthenticationOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }
}
