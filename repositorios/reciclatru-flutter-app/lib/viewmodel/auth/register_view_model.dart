/// Coordina las acciones y el estado de register view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/auth_module.dart';
import '../../model/session/session.dart';
import 'register_ui_state.dart';

/// Mantiene el flujo Kotlin: registrar y autenticar automáticamente.
final registerViewModelProvider =
    NotifierProvider.autoDispose<RegisterViewModel, RegisterUiState>(RegisterViewModel.new);

class RegisterViewModel extends Notifier<RegisterUiState> {
  @override
  RegisterUiState build() => const RegisterUiState();

  /// Cambios de formulario; cada cambio borra los errores de ese campo.
  void onEmailChange(String value) => state = state.copyWith(
    email: value,
    clearEmailError: true,
    clearGeneralError: true,
  );

  void onPasswordChange(String value) => state = state.copyWith(
    password: value,
    clearPasswordError: true,
    clearGeneralError: true,
  );

  void onTogglePasswordVisibility() => state = state.copyWith(
    passwordVisible: !state.passwordVisible,
  );

  void onRoleSelect(UserRole role) => state = state.copyWith(
    role: role,
    clearRoleError: true,
    clearGeneralError: true,
  );

  /// Registra una sola vez; si el ingreso falla conserva la cuenta creada para reintentar.
  Future<void> onSubmit() async {
    if (state.submitting) return;
    final email = state.email.trim();
    final emailError = email.isEmpty
        ? 'Ingresa tu correo electrónico.'
        : !RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email)
            ? 'Ingresa un correo válido.'
            : null;
    final passwordError = state.password.length < 8
        ? 'Debe tener al menos 8 caracteres.'
        : null;
    final roleError = state.role == null
        ? 'Elige cómo usarás ReciclaTru.'
        : null;
    if (emailError != null || passwordError != null || roleError != null) {
      state = state.copyWith(
        emailError: emailError,
        clearEmailError: emailError == null,
        passwordError: passwordError,
        clearPasswordError: passwordError == null,
        roleError: roleError,
        clearRoleError: roleError == null,
        clearGeneralError: true,
      );
      return;
    }

    final role = state.role!;
    final password = state.password;
    final wasCreated = state.accountCreated;
    state = state.copyWith(submitting: true, clearGeneralError: true);
    try {
      if (!wasCreated) {
        await ref.read(registerUseCaseProvider).call(email, password, role.apiValue);
        state = state.copyWith(accountCreated: true);
      }
      final result = await ref.read(loginUseCaseProvider).call(email, password);
      state = state.copyWith(
        submitting: false,
        registeredRole: result.session.role,
      );
    } catch (error) {
      final network = error is NetworkException ? error : null;
      if (network?.kind == NetworkErrorKind.conflict && !wasCreated) {
        state = state.copyWith(
          submitting: false,
          emailError: 'Ya existe una cuenta con este correo',
        );
      } else if (network?.statusCode == 429) {
        state = state.copyWith(
          submitting: false,
          generalError: 'Hiciste muchos intentos. Espera un momento e inténtalo de nuevo.',
        );
      } else if (state.accountCreated) {
        state = state.copyWith(
          submitting: false,
          generalError: 'Tu cuenta se creó, pero no pudimos ingresar. Toca Crear cuenta para intentarlo de nuevo.',
        );
      } else {
        state = state.copyWith(submitting: false, generalError: toUserMessage(error));
      }
    }
  }
}
