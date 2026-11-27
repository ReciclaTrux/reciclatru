/// Define los datos de interfaz de login y cómo se actualiza su estado.
import '../../model/session/session.dart';

/// Estado de la pantalla de ingreso, equivalente a `LoginUiState.kt`.
class LoginUiState {
  const LoginUiState({
    this.email = '',
    this.password = '',
    this.passwordVisible = false,
    this.sessionExpired = false,
    this.emailError,
    this.passwordError,
    this.generalError,
    this.submitting = false,
    this.loggedInRole,
    this.needsProfile = false,
  });

  final String email;
  final String password;
  final bool passwordVisible;
  final bool sessionExpired;
  final String? emailError;
  final String? passwordError;
  final String? generalError;
  final bool submitting;
  final UserRole? loggedInRole;
  final bool needsProfile;

  /// Crea una copia inmutable y permite borrar errores de forma explícita.
  LoginUiState copyWith({
    String? email,
    String? password,
    bool? passwordVisible,
    bool? sessionExpired,
    String? emailError,
    bool clearEmailError = false,
    String? passwordError,
    bool clearPasswordError = false,
    String? generalError,
    bool clearGeneralError = false,
    bool? submitting,
    UserRole? loggedInRole,
    bool? needsProfile,
  }) => LoginUiState(
    email: email ?? this.email,
    password: password ?? this.password,
    passwordVisible: passwordVisible ?? this.passwordVisible,
    sessionExpired: sessionExpired ?? this.sessionExpired,
    emailError: clearEmailError ? null : emailError ?? this.emailError,
    passwordError: clearPasswordError ? null : passwordError ?? this.passwordError,
    generalError: clearGeneralError ? null : generalError ?? this.generalError,
    submitting: submitting ?? this.submitting,
    loggedInRole: loggedInRole ?? this.loggedInRole,
    needsProfile: needsProfile ?? this.needsProfile,
  );
}
