/// Transforma la respuesta de user profile mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/profile/user_profile.dart';
import '../../dto/profile/party_profile_dtos.dart';

/// Traduce la salida remota del perfil al modelo de dominio.
UserProfile mapUserProfile(PartyProfileOutputDto dto) => UserProfile.fromJson(dto.json);
