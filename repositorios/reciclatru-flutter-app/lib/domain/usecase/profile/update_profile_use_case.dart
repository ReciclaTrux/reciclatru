/// Encapsula el caso de uso update profile use case y delega la operación en los contratos del dominio.
import '../../repository/profile/user_profile_repository.dart';
import '../../../model/profile/user_profile.dart';

/// Guarda los datos personales del perfil.
class UpdateProfileUseCase {
  const UpdateProfileUseCase(this._repository);
  final UserProfileRepository _repository;
  Future<UserProfile> call({required String fullName, required String phone, required String? reference}) =>
      _repository.updateProfile(fullName: fullName, phone: phone, reference: reference);
}
