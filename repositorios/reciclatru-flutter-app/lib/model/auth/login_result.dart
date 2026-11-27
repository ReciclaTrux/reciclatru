// Este modelo guarda el resultado del inicio de sesión (la sesión activa del usuario y si ya completó su perfil).
import 'package:reciclatru/model/session/session.dart';

// Esta clase contiene los datos que devuelve el inicio de sesión exitoso.
class LoginResult {
  const LoginResult({required this.session, required this.profileComplete});
  final Session session;
  final bool profileComplete;
}
