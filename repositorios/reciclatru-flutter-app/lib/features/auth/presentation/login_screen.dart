
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'auth_provider.dart';

import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/theme/rt_spacing.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  String _email = '';
  String _password = '';

  bool _obscurePassword = true;

  String? _errorMessage;
  String? _successMessage;

  Future<void> _handleLogin() async {
    final email = _email.trim();
    final password = _password;

    setState(() {
      _errorMessage = null;
      _successMessage = null;
    });

    if (email.isEmpty || password.isEmpty) {
      setState(() {
        _errorMessage = 'Por favor, completa todos los campos.';
      });
      return;
    }

    ref.read(authLoadingProvider.notifier).isLoading = true;

    try {
      final authRepo = ref.read(authRepositoryProvider);

      final response = await authRepo.login(email, password);

      ref.read(authUserProvider.notifier).user = response.user;

      if (!mounted) return;

      setState(() {
        _successMessage = '¡Bienvenido, ${response.user.email}!';
      });

    } catch (e) {
      if (!mounted) return;

      setState(() {
        _errorMessage = e.toString().replaceAll('Exception: ', '');
      });

    } finally {
      if (mounted) {
        ref.read(authLoadingProvider.notifier).isLoading = false;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isLoading = ref.watch(authLoadingProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Iniciar sesión'),
      ),

      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(RtSpacing.s4),

          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              const SizedBox(height: 30),

              Icon(
                Icons.recycling,
                size: 75,
                color: theme.colorScheme.primary,
              ),

              const SizedBox(height: 20),

              Text(
                '¡Bienvenido a ReciclaTru!',
                textAlign: TextAlign.center,
                style: theme.textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 8),

              Text(
                'Ingresa a tu cuenta para continuar.',
                textAlign: TextAlign.center,
                style: theme.textTheme.bodyLarge,
              ),

              const SizedBox(height: 35),

              // Correo electrónico
              RtTextField(
                label: 'Correo electrónico',
                value: _email,
                placeholder: 'ejemplo@correo.com',
                keyboardType: TextInputType.emailAddress,
                leadingIcon: Icons.email_outlined,
                onValueChange: (value) {
                  setState(() {
                    _email = value;
                    _errorMessage = null;
                  });
                },
              ),

              const SizedBox(height: 22),

              // Contraseña
              RtTextField(
                label: 'Contraseña',
                value: _password,
                placeholder: 'Ingresa tu contraseña',
                leadingIcon: Icons.lock_outline,
                obscureText: _obscurePassword,
                trailingIcon: _obscurePassword
                    ? Icons.visibility_outlined
                    : Icons.visibility_off_outlined,
                trailingIconDescription: _obscurePassword
                    ? 'Mostrar contraseña'
                    : 'Ocultar contraseña',
                onTrailingIconClick: () {
                  setState(() {
                    _obscurePassword = !_obscurePassword;
                  });
                },
                onValueChange: (value) {
                  setState(() {
                    _password = value;
                    _errorMessage = null;
                  });
                },
              ),

              const SizedBox(height: 25),

              // Mensaje de error
              if (_errorMessage != null) ...[
                RtInlineAlert(
                  tone: RtInlineAlertTone.error,
                  message: _errorMessage!,
                ),
                const SizedBox(height: 20),
              ],

              // Mensaje de éxito
              if (_successMessage != null) ...[
                RtInlineAlert(
                  tone: RtInlineAlertTone.success,
                  message: _successMessage!,
                ),
                const SizedBox(height: 20),
              ],

              // Botón iniciar sesión
              RtButton(
                text: isLoading
                    ? 'Iniciando sesión…'
                    : 'Iniciar sesión',
                fullWidth: true,
                loading: isLoading,
                enabled: !isLoading,
                icon: Icons.login,
                onClick: _handleLogin,
              ),

              const SizedBox(height: 25),

              const Divider(),

              const SizedBox(height: 15),

              Text(
                '¿Todavía no tienes una cuenta?',
                textAlign: TextAlign.center,
                style: theme.textTheme.bodyMedium,
              ),

              const SizedBox(height: 10),

              RtButton(
                text: 'Crear una cuenta',
                variant: RtButtonVariant.secondary,
                fullWidth: true,
                onClick: () {
                  Navigator.pushReplacementNamed(
                    context,
                    '/register',
                  );
                },
              ),
            ],
          ),
        ),
      ),
    );
  }
}
