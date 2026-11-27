/// Construye la pantalla o navegación de register screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_text_field.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_inline_alert.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_role_selector.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';
import '../../model/session/session.dart';
import '../../viewmodel/auth/register_view_model.dart';

/// Traducción exacta de RegisterContent / RegisterViewModel de Android (HU-01).
/// Al crear la cuenta se autentica automáticamente y pasa a completar el perfil.
class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  Future<void> _handleRegister() async {
    await ref.read(registerViewModelProvider.notifier).onSubmit();
    if (!mounted) return;
    if (ref.read(registerViewModelProvider).registeredRole != null) {
      context.go('/complete-profile');
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final uiState = ref.watch(registerViewModelProvider);
    final viewModel = ref.read(registerViewModelProvider.notifier);

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      bottomNavigationBar: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(
            horizontal: RtSpacing.s4, vertical: RtSpacing.s3),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              RtButton(
                key: const Key('btn_crear_cuenta'),
                text: uiState.submitting ? 'Creando cuenta…' : 'Crear cuenta',
                onClick: _handleRegister,
                fullWidth: true,
                loading: uiState.submitting,
                enabled: !uiState.submitting,
              ),
              const SizedBox(height: RtSpacing.s1),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '¿Ya tienes cuenta?',
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  RtButton(
                    key: const Key('btn_ir_a_login'),
                    text: 'Inicia sesión',
                    onClick: () => context.go('/login'),
                    variant: RtButtonVariant.text,
                    size: RtButtonSize.compact,
                    enabled: !uiState.submitting,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(RtSpacing.s4),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const SizedBox(height: RtSpacing.s4),
              Text(
                'ReciclaTru',
                style: theme.textTheme.labelLarge?.copyWith(
                  color: theme.colorScheme.primary,
                ),
              ),
              const SizedBox(height: RtSpacing.s1),
              Text(
                'Crea tu cuenta',
                style: theme.textTheme.headlineSmall,
              ),
              const SizedBox(height: RtSpacing.s1),
              Text(
                'Conecta tu material reciclable con quien lo recoge.',
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: RtSpacing.s4),
              if (uiState.generalError != null) ...[
                RtInlineAlert(
                  key: const Key('alert_registro_error'),
                  tone: RtInlineAlertTone.error,
                  message: uiState.generalError!,
                ),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtTextField(
                key: const Key('input_correo'),
                label: 'Correo electrónico',
                value: uiState.email,
                placeholder: 'nombre@correo.com',
                keyboardType: TextInputType.emailAddress,
                leadingIcon: Icons.mail_rounded,
                error: uiState.emailError,
                onValueChange: viewModel.onEmailChange,
              ),
              const SizedBox(height: RtSpacing.s4),
              RtTextField(
                key: const Key('input_contrasena'),
                label: 'Contraseña',
                value: uiState.password,
                placeholder: 'Tu contraseña',
                helper: 'Mínimo 8 caracteres',
                leadingIcon: Icons.lock_rounded,
                obscureText: !uiState.passwordVisible,
                error: uiState.passwordError,
                trailingIcon: uiState.passwordVisible
                    ? Icons.visibility_off_rounded
                    : Icons.visibility_rounded,
                trailingIconDescription: uiState.passwordVisible
                    ? 'Ocultar contraseña'
                    : 'Mostrar contraseña',
                onTrailingIconClick: viewModel.onTogglePasswordVisibility,
                onValueChange: viewModel.onPasswordChange,
              ),
              const SizedBox(height: RtSpacing.s4),
              RtRoleSelector(
                key: const Key('selector_rol'),
                selected: uiState.role == UserRole.neighbor
                    ? RtRole.neighbor
                    : uiState.role == UserRole.recycler
                        ? RtRole.recycler
                        : null,
                error: uiState.roleError,
                onSelect: (role) => viewModel.onRoleSelect(
                  role == RtRole.neighbor ? UserRole.neighbor : UserRole.recycler,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
