/// Define los datos de interfaz de register y cómo se actualiza su estado.
import '../../model/session/session.dart';

/// Estado de registro migrado desde `RegisterUiState.kt`.
class RegisterUiState {
  const RegisterUiState({
    this.email = '',
    this.password = '',
    this.passwordVisible = false,
    this.role,
    this.emailError,
    this.passwordError,
    this.roleError,
    this.generalError,
    this.submitting = false,
    this.accountCreated = false,
    this.registeredRole,
  });

  final String email;
  final String password;
  final bool passwordVisible;
  final UserRole? role;
  final String? emailError;
  final String? passwordError;
  final String? roleError;
  final String? generalError;
  final bool submitting;
  final bool accountCreated;
  final UserRole? registeredRole;

  /// Copia inmutable con controles para limpiar errores opcionales.
  RegisterUiState copyWith({
    String? email,
    String? password,
    bool? passwordVisible,
    UserRole? role,
    bool clearRole = false,
    String? emailError,
    bool clearEmailError = false,
    String? passwordError,
    bool clearPasswordError = false,
    String? roleError,
    bool clearRoleError = false,
    String? generalError,
    bool clearGeneralError = false,
    bool? submitting,
    bool? accountCreated,
    UserRole? registeredRole,
  }) => RegisterUiState(
    email: email ?? this.email,
    password: password ?? this.password,
    passwordVisible: passwordVisible ?? this.passwordVisible,
    role: clearRole ? null : role ?? this.role,
    emailError: clearEmailError ? null : emailError ?? this.emailError,
    passwordError: clearPasswordError ? null : passwordError ?? this.passwordError,
    roleError: clearRoleError ? null : roleError ?? this.roleError,
    generalError: clearGeneralError ? null : generalError ?? this.generalError,
    submitting: submitting ?? this.submitting,
    accountCreated: accountCreated ?? this.accountCreated,
    registeredRole: registeredRole ?? this.registeredRole,
  );
}
