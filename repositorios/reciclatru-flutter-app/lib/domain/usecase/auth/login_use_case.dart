// Este caso de uso realiza el inicio de sesión del usuario comprobando su correo y contraseña.
import '../../repository/auth/auth_repository.dart';
import '../../../model/auth/login_result.dart';

// Esta clase ejecuta la acción de inicio de sesión llamando al repositorio de autenticación.
class LoginUseCase {
  const LoginUseCase(this._repository);
  final AuthRepository _repository;
  Future<LoginResult> call(String email, String password) => _repository.login(email, password);
}
