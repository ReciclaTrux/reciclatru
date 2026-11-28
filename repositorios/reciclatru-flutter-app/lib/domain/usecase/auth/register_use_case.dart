// Este caso de uso registra una nueva cuenta en el sistema con el correo, contraseña y rol elegidos.
import '../../repository/auth/auth_repository.dart';

// Esta clase ejecuta el registro de la nueva cuenta a través del repositorio de autenticación.
class RegisterUseCase {
  const RegisterUseCase(this._repository);
  final AuthRepository _repository;
  Future<void> call(String email, String password, String role) => _repository.register(email, password, role);
}
