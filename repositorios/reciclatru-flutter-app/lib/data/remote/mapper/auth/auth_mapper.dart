/// Transforma la respuesta de auth mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/auth/login_result.dart';
import '../../../../model/session/session.dart';
import '../../dto/auth/auth_dtos.dart';

/// Mapea las respuestas HTTP al modelo de sesión de dominio.
LoginResult toLoginResult(PartyAuthenticationOutputDto dto) {
  final record = dto.record;
  final token = dto.accessToken;
  final userId = dto.crReferenceId ?? record?.id;
  final role = UserRole.fromApiValue(record?.role);
  if (token == null || token.isEmpty || userId == null || userId.isEmpty || role == null) {
    throw const FormatException('No pudimos completar la operación. Inténtalo de nuevo.');
  }
  return LoginResult(
    session: Session(userId: userId, token: token, role: role),
    profileComplete: record?.profileComplete == true,
  );
}
