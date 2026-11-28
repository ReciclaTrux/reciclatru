/// Implementa el repositorio user profile repository impl conectando los contratos del dominio con sus fuentes de datos.
import '../../../domain/repository/profile/user_profile_repository.dart';
import '../../../model/profile/user_profile.dart';
import '../../remote/api/profile/profile_api.dart';
import '../../remote/dto/profile/party_profile_dtos.dart';
import '../../remote/mapper/profile/user_profile_mapper.dart';
import '../../remote/network_exception.dart';

/// Consulta y actualiza los datos personales como `UserProfileRepositoryImpl.kt`.
class UserProfileRepositoryImpl implements UserProfileRepository {
  const UserProfileRepositoryImpl(this._api);
  final ProfileApi _api;

  @override
  Future<UserProfile> getProfile() async => mapUserProfile(
    await apiCall(() => _api.getProfile()),
  );

  @override
  Future<UserProfile> updateProfile({
    required String fullName,
    required String phone,
    required String? reference,
  }) async {
    final response = await apiCall(() => _api.updateProfile(ProfileUpdateRequestDto(
      fullName: fullName.trim(),
      phone: phone.trim(),
      reference: reference?.trim(),
    )));
    return mapUserProfile(response);
  }
}
