/// Encapsula el caso de uso get profile use case y delega la operación en los contratos del dominio.
import '../../repository/profile/user_profile_repository.dart';
import '../../../model/profile/user_profile.dart';

/// Recupera el perfil asociado con la sesión activa.
class GetProfileUseCase {
  const GetProfileUseCase(this._repository);
  final UserProfileRepository _repository;
  Future<UserProfile> call() => _repository.getProfile();
}
