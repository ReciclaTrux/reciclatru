import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/molecules/rt_role_selector.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import 'auth_provider.dart';

/// Traducción exacta de RegisterContent / RegisterViewModel de Android (HU-01).
/// Al crear la cuenta se autentica automáticamente y pasa a completar el perfil.
class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() => _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  bool _submitting = false;
  String _email = '';
  String _password = '';
  RtRole? _selectedRole;
  bool _passwordVisible = false;

  String? _emailError;
  String? _passwordError;
  String? _roleError;
  String? _generalError;

  Future<void> _handleRegister() async {
    if (_submitting) return;

    final email = _email.trim();
    final emailError = email.isEmpty
        ? 'Ingresa tu correo electrónico.'
        : !RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email)
            ? 'Ingresa un correo válido.'
            : null;

    final passwordError = _password.length < 8
        ? 'Debe tener al menos 8 caracteres.'
        : null;

    final roleError = _selectedRole == null
        ? 'Elige cómo usarás ReciclaTru.'
        : null;

    setState(() {
      _emailError = emailError;
      _passwordError = passwordError;
      _roleError = roleError;
      _generalError = null;
    });

    if (emailError != null || passwordError != null || roleError != null) {
      return;
    }

    final role = _selectedRole == RtRole.neighbor ? 'NEIGHBOR' : 'RECYCLER';
    setState(() => _submitting = true);

    try {
      final authRepo = ref.read(authRepositoryProvider);
      await authRepo.register(email, _password, role);
      await authRepo.login(email, _password);

      if (!mounted) return;
      context.go('/complete-profile');
    } catch (e) {
      if (!mounted) return;
      final message = e.toString().replaceFirst('Exception: ', '');
      setState(() {
        if (message == 'Ya existe una cuenta con este correo') {
          _emailError = message;
        } else {
          _generalError = message;
        }
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
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
                text: _submitting ? 'Creando cuenta…' : 'Crear cuenta',
                onClick: _handleRegister,
                fullWidth: true,
                loading: _submitting,
                enabled: !_submitting,
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
                    enabled: !_submitting,
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
              if (_generalError != null) ...[
                RtInlineAlert(
                  key: const Key('alert_registro_error'),
                  tone: RtInlineAlertTone.error,
                  message: _generalError!,
                ),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtTextField(
                key: const Key('input_correo'),
                label: 'Correo electrónico',
                value: _email,
                placeholder: 'nombre@correo.com',
                keyboardType: TextInputType.emailAddress,
                leadingIcon: Icons.mail_rounded,
                error: _emailError,
                onValueChange: (value) {
                  setState(() {
                    _email = value;
                    _emailError = null;
                    _generalError = null;
                  });
                },
              ),
              const SizedBox(height: RtSpacing.s4),
              RtTextField(
                key: const Key('input_contrasena'),
                label: 'Contraseña',
                value: _password,
                placeholder: 'Tu contraseña',
                helper: 'Mínimo 8 caracteres',
                leadingIcon: Icons.lock_rounded,
                obscureText: !_passwordVisible,
                error: _passwordError,
                trailingIcon: _passwordVisible
                    ? Icons.visibility_off_rounded
                    : Icons.visibility_rounded,
                trailingIconDescription: _passwordVisible
                    ? 'Ocultar contraseña'
                    : 'Mostrar contraseña',
                onTrailingIconClick: () {
                  setState(() {
                    _passwordVisible = !_passwordVisible;
                  });
                },
                onValueChange: (value) {
                  setState(() {
                    _password = value;
                    _passwordError = null;
                    _generalError = null;
                  });
                },
              ),
              const SizedBox(height: RtSpacing.s4),
              RtRoleSelector(
                key: const Key('selector_rol'),
                selected: _selectedRole,
                error: _roleError,
                onSelect: (role) {
                  setState(() {
                    _selectedRole = role;
                    _roleError = null;
                    _generalError = null;
                  });
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
