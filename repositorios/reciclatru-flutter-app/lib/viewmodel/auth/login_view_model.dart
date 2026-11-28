/// Coordina las acciones y el estado de login view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/auth_module.dart';
import '../../model/session/session.dart';
import 'login_ui_state.dart';

/// Valida el ingreso y ejecuta LoginUseCase como el ViewModel Kotlin.
final loginViewModelProvider = NotifierProvider.autoDispose<LoginViewModel, LoginUiState>(
  LoginViewModel.new,
);

class LoginViewModel extends Notifier<LoginUiState> {
  @override
  LoginUiState build() => const LoginUiState();

  /// Actualiza correo y limpia los mensajes asociados al intento previo.
  void onEmailChange(String value) => state = state.copyWith(
    email: value,
    clearEmailError: true,
    clearGeneralError: true,
  );

  /// Actualiza contraseña y limpia sus errores.
  void onPasswordChange(String value) => state = state.copyWith(
    password: value,
    clearPasswordError: true,
    clearGeneralError: true,
  );

  /// Alterna la visibilidad del campo sin modificar su valor.
  void onTogglePasswordVisibility() => state = state.copyWith(
    passwordVisible: !state.passwordVisible,
  );

  /// Valida los campos localmente y luego envía las credenciales al caso de uso.
  Future<void> onSubmit() async {
    if (state.submitting) return;
    final email = state.email.trim();
    final emailError = email.isEmpty
        ? 'Ingresa tu correo electrónico.'
        : !RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email)
            ? 'Ingresa un correo válido.'
            : null;
    final passwordError = state.password.isEmpty
        ? 'Ingresa tu contraseña.'
        : null;
    if (emailError != null || passwordError != null) {
      state = state.copyWith(
        emailError: emailError,
        clearEmailError: emailError == null,
        passwordError: passwordError,
        clearPasswordError: passwordError == null,
        clearGeneralError: true,
      );
      return;
    }

    final password = state.password;
    state = state.copyWith(submitting: true, clearGeneralError: true);
    try {
      final result = await ref.read(loginUseCaseProvider).call(email, password);
      state = state.copyWith(
        submitting: false,
        loggedInRole: result.session.role,
        needsProfile: !result.profileComplete,
      );
    } catch (error) {
      final network = error is NetworkException ? error : null;
      final message = network?.kind == NetworkErrorKind.unauthorized ||
              network?.statusCode == 400
          ? 'Correo o contraseña incorrectos.'
          : network?.statusCode == 429
              ? 'Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.'
              : toUserMessage(error);
      state = state.copyWith(submitting: false, generalError: message);
    }
  }
}
