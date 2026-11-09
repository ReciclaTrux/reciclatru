import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/theme/rt_radii.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import 'auth_provider.dart';

/// Traducción de LoginContent / LoginViewModel de Android HU-02.
/// Textos, orden y botón fijo inferior siguen LoginScreen.kt.
class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key, this.sessionExpired = false});
  final bool sessionExpired;
  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  String _email = '';
  String _password = '';
  bool _passwordVisible = false;
  bool _submitting = false;
  String? _emailError;
  String? _passwordError;
  String? _generalError;

  Future<void> _submit() async {
    if (_submitting) return;
    final email = _email.trim();
    final emailError = email.isEmpty
        ? 'Ingresa tu correo electrónico.'
        : !RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(email)
            ? 'Ingresa un correo válido.'
            : null;
    final passwordError = _password.isEmpty ? 'Ingresa tu contraseña.' : null;
    setState(() {
      _emailError = emailError;
      _passwordError = passwordError;
      _generalError = null;
    });
    if (emailError != null || passwordError != null) return;
    setState(() => _submitting = true);
    try {
      await ref.read(authRepositoryProvider).login(email, _password);
      if (!mounted) return;
      // SessionGate reacciona a SessionStore.save y muestra el destino.
      Navigator.of(context).popUntil((route) => route.isFirst);
    } catch (error) {
      if (!mounted) return;
      setState(() => _generalError = error.toString().replaceFirst('Exception: ', ''));
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
                text: _submitting ? 'Ingresando…' : 'Ingresar',
                onClick: _submit,
                loading: _submitting,
                fullWidth: true,
              ),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Flexible(child: Text('¿Aún no tienes cuenta?',
                    style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant))),
                  RtButton(
                    text: 'Regístrate',
                    onClick: () => Navigator.pushReplacementNamed(context, '/register'),
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
              const SizedBox(height: RtSpacing.s5),
              Container(
                width: 52,
                height: 52,
                decoration: BoxDecoration(
                  color: theme.colorScheme.primary,
                  borderRadius: BorderRadius.circular(RtRadii.md),
                ),
                child: Icon(Icons.recycling_rounded, color: theme.colorScheme.onPrimary),
              ),
              const SizedBox(height: RtSpacing.s4),
              Text('Hola de nuevo', style: theme.textTheme.headlineSmall),
              const SizedBox(height: RtSpacing.s1),
              Text('Ingresa con tu correo y contraseña.',
                style: theme.textTheme.bodyLarge?.copyWith(color: theme.colorScheme.onSurfaceVariant)),
              const SizedBox(height: RtSpacing.s4),
              if (_generalError != null) ...[
                RtInlineAlert(tone: RtInlineAlertTone.error, message: _generalError!),
                const SizedBox(height: RtSpacing.s4),
              ] else if (widget.sessionExpired) ...[
                const RtInlineAlert(tone: RtInlineAlertTone.info,
                  message: 'Tu sesión expiró. Ingresa de nuevo para continuar.'),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtTextField(
                label: 'Correo electrónico',
                value: _email,
                onValueChange: (value) => setState(() {
                  _email = value;
                  _emailError = null;
                  _generalError = null;
                }),
                error: _emailError,
                placeholder: 'nombre@correo.com',
                leadingIcon: Icons.mail_rounded,
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: RtSpacing.s4),
              RtTextField(
                label: 'Contraseña',
                value: _password,
                onValueChange: (value) => setState(() {
                  _password = value;
                  _passwordError = null;
                  _generalError = null;
                }),
                error: _passwordError,
                placeholder: 'Tu contraseña',
                leadingIcon: Icons.lock_rounded,
                keyboardType: TextInputType.visiblePassword,
                obscureText: !_passwordVisible,
                trailingIcon: _passwordVisible ? Icons.visibility_off_rounded : Icons.visibility_rounded,
                trailingIconDescription: _passwordVisible ? 'Ocultar contraseña' : 'Mostrar contraseña',
                onTrailingIconClick: () => setState(() => _passwordVisible = !_passwordVisible),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
