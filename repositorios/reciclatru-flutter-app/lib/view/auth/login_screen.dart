/// Construye la pantalla o navegación de login screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_text_field.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_inline_alert.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/auth/login_view_model.dart';

/// Traducción exacta de LoginContent / LoginViewModel de Android (HU-02).
/// Mantiene la maquetación, textos, ícono, alertas y testTags de LoginScreen.kt.
class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key, this.sessionExpired = false});
  final bool sessionExpired;

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  Future<void> _submit() async {
    await ref.read(loginViewModelProvider.notifier).onSubmit();
    if (!mounted) return;
    final state = ref.read(loginViewModelProvider);
    if (state.loggedInRole == null) return;
    context.go(state.needsProfile ? '/complete-profile' : '/home');
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final uiState = ref.watch(loginViewModelProvider);
    final viewModel = ref.read(loginViewModelProvider.notifier);
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
                key: const Key('btn_ingresar'),
                text: uiState.submitting ? 'Ingresando…' : 'Ingresar',
                onClick: _submit,
                loading: uiState.submitting,
                fullWidth: true,
              ),
              const SizedBox(height: RtSpacing.s1),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    '¿Aún no tienes cuenta?',
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  RtButton(
                    key: const Key('btn_ir_a_registro'),
                    text: 'Regístrate',
                    onClick: () => context.go('/register'),
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
              const SizedBox(height: RtSpacing.s5),
              Container(
                width: RtSize.thumbSm,
                height: RtSize.thumbSm,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: theme.colorScheme.primary,
                  borderRadius: BorderRadius.circular(RtRadii.md),
                ),
                child: Icon(
                  Icons.recycling_rounded,
                  color: theme.colorScheme.onPrimary,
                  size: RtSize.iconMd,
                ),
              ),
              const SizedBox(height: RtSpacing.s4),
              Text(
                'Hola de nuevo',
                style: theme.textTheme.headlineSmall,
              ),
              const SizedBox(height: RtSpacing.s1),
              Text(
                'Ingresa con tu correo y contraseña.',
                style: theme.textTheme.bodyLarge?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              const SizedBox(height: RtSpacing.s4),
              if (uiState.generalError != null) ...[
                RtInlineAlert(
                  key: const Key('alert_login_error'),
                  tone: RtInlineAlertTone.error,
                  message: uiState.generalError!,
                ),
                const SizedBox(height: RtSpacing.s4),
              ] else if (widget.sessionExpired) ...[
                const RtInlineAlert(
                  key: Key('alert_sesion_expirada'),
                  tone: RtInlineAlertTone.info,
                  message: 'Tu sesión expiró. Ingresa de nuevo para continuar.',
                ),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtTextField(
                key: const Key('input_correo'),
                label: 'Correo electrónico',
                value: uiState.email,
                onValueChange: viewModel.onEmailChange,
                error: uiState.emailError,
                placeholder: 'nombre@correo.com',
                leadingIcon: Icons.mail_rounded,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: RtSpacing.s4),
              RtTextField(
                key: const Key('input_contrasena'),
                label: 'Contraseña',
                value: uiState.password,
                onValueChange: viewModel.onPasswordChange,
                error: uiState.passwordError,
                placeholder: 'Tu contraseña',
                leadingIcon: Icons.lock_rounded,
                keyboardType: TextInputType.visiblePassword,
                obscureText: !uiState.passwordVisible,
                trailingIcon: uiState.passwordVisible
                    ? Icons.visibility_off_rounded
                    : Icons.visibility_rounded,
                trailingIconDescription: uiState.passwordVisible
                    ? 'Ocultar contraseña'
                    : 'Mostrar contraseña',
                onTrailingIconClick: viewModel.onTogglePasswordVisibility,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
