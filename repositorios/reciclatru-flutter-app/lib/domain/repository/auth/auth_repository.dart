// Este contrato define las funciones necesarias para registrar usuarios, iniciar sesión y cerrar sesión.
import '../../../model/auth/login_result.dart';

// Esta interfaz especifica las operaciones de autenticación de cuentas en la aplicación.
abstract interface class AuthRepository {
  Future<void> register(String email, String password, String role);
  Future<LoginResult> login(String email, String password);
  Future<void> logout();
}
