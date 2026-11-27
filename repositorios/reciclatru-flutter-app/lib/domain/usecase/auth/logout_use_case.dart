// Este caso de uso realiza el cierre de la sesión activa en la aplicación.
import '../../repository/auth/auth_repository.dart';

// Esta clase ejecuta la salida del usuario limpiando sus datos de sesión guardados.
class LogoutUseCase {
  const LogoutUseCase(this._repository);
  final AuthRepository _repository;
  Future<void> call() => _repository.logout();
}
