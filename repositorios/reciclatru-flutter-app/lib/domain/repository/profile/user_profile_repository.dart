// Este contrato define las funciones para consultar y actualizar la información personal del usuario.
import '../../../model/profile/user_profile.dart';

// Esta interfaz especifica cómo obtener y actualizar los datos personales del usuario.
abstract interface class UserProfileRepository {
  Future<UserProfile> getProfile();
  Future<UserProfile> updateProfile({
    required String fullName,
    required String phone,
    required String? reference,
  });
}
