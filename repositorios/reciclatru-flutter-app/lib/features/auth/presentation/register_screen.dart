
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'auth_provider.dart';

// Design System oficial de ReciclaTru
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/molecules/rt_role_selector.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/molecules/rt_snackbar.dart';
import '../../../core/design_system/theme/rt_spacing.dart';

class RegisterScreen extends ConsumerStatefulWidget {
  const RegisterScreen({super.key});

  @override
  ConsumerState<RegisterScreen> createState() =>
      _RegisterScreenState();
}

class _RegisterScreenState extends ConsumerState<RegisterScreen> {
  bool _submitting = false;

  // Datos del formulario
  String _email = '';
  String _password = '';

  // Rol seleccionado
  RtRole? _selectedRole;

  // Visibilidad de contraseña
  bool _obscurePassword = true;

  // Errores de validación
  String? _emailError;
  String? _passwordError;
  String? _roleError;
  String? _formError;

  // Validar y registrar usuario
  Future<void> _handleRegister() async {
    if (_submitting) return;

    // Limpiar errores anteriores
    setState(() {
      _emailError = null;
      _passwordError = null;
      _roleError = null;
      _formError = null;
    });

    bool hasError = false;

    final email = _email.trim().toLowerCase();

    // Validar correo
    if (email.isEmpty) {
      _emailError = 'Ingresa tu correo electrónico.';
      hasError = true;
    } else {
      final emailRegex =
          RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$');

      if (!emailRegex.hasMatch(email)) {
        _emailError = 'Ingresa un correo válido.';
        hasError = true;
      }
    }

    // Validar contraseña
    if (_password.isEmpty) {
      _passwordError = 'La contraseña es obligatoria.';
      hasError = true;
    } else if (_password.length < 8) {
      _passwordError =
          'Debe tener al menos 8 caracteres.';
      hasError = true;
    }

    // Validar selección de rol
    if (_selectedRole == null) {
      _roleError = 'Elige cómo usarás ReciclaTru.';
      hasError = true;
    }

    setState(() {});

    if (hasError) return;

    // Convertir el enum al valor esperado por el backend
    final role = _selectedRole == RtRole.neighbor
        ? 'NEIGHBOR'
        : 'RECYCLER';

    // Impedir envíos duplicados.
    setState(() => _submitting = true);

    try {
      final authRepo = ref.read(authRepositoryProvider);

      // Conectar con el backend
      await authRepo.register(
        email,
        _password,
        role,
      );

      if (!mounted) return;

      // HU-02 completará el login automático y la sesión persistente.
      // En esta entrega HU-01 se abre el login, sin registrar dos veces.
      showRtSnackbar(
        context,
        message: 'Cuenta creada. Inicia sesión para continuar.',
      );
      Navigator.pushReplacementNamed(context, '/login');
    } catch (e) {
      if (!mounted) return;

      final message = e.toString().replaceFirst('Exception: ', '');
      setState(() {
        if (message == 'Ya existe una cuenta con este correo') {
          _emailError = message;
        } else {
          _formError = message;
        }
      });
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final isLoading = _submitting;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Crear cuenta'),
        elevation: 0,
      ),

      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(RtSpacing.s4),

          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Encabezado
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

              const SizedBox(height: RtSpacing.s2),

              Text(
                'Conecta tu material reciclable con quienes '
                'pueden darle una nueva oportunidad.',
                style: theme.textTheme.bodyMedium?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),

              const SizedBox(height: RtSpacing.s6),

              // Correo electrónico
              RtTextField(
                label: 'Correo electrónico',
                value: _email,
                placeholder: 'nombre@correo.com',
                keyboardType: TextInputType.emailAddress,
                leadingIcon: Icons.email_outlined,
                error: _emailError,
                onValueChange: (value) {
                  setState(() {
                    _email = value;
                    _emailError = null;
                    _formError = null;
                  });
                },
              ),

              const SizedBox(height: RtSpacing.s4),

              // Contraseña
              RtTextField(
                label: 'Contraseña',
                value: _password,
                placeholder: 'Mínimo 8 caracteres',
                leadingIcon: Icons.lock_outline,
                obscureText: _obscurePassword,
                error: _passwordError,
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
                    _passwordError = null;
                    _formError = null;
                  });
                },
              ),

              const SizedBox(height: RtSpacing.s6),

              // Selector de rol oficial
              RtRoleSelector(
                selected: _selectedRole,
                error: _roleError,
                onSelect: (role) {
                  setState(() {
                    _selectedRole = role;
                    _roleError = null;
                    _formError = null;
                  });
                },
              ),

              const SizedBox(height: RtSpacing.s4),

              // Error devuelto por el backend
              if (_formError != null) ...[
                RtInlineAlert(
                  tone: RtInlineAlertTone.error,
                  title: 'No se pudo crear la cuenta',
                  message: _formError!,
                ),

                const SizedBox(height: RtSpacing.s4),
              ],

              // Botón de registro
              RtButton(
                text: isLoading
                    ? 'Creando cuenta…'
                    : 'Crear cuenta',
                onClick: _handleRegister,
                fullWidth: true,
                loading: isLoading,
                enabled: !isLoading,
              ),

              const SizedBox(height: RtSpacing.s4),

              // Navegación equivalente al enlace de Android.
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text('¿Ya tienes cuenta?'),
                  TextButton(
                    onPressed: isLoading
                        ? null
                        : () => Navigator.pushReplacementNamed(context, '/login'),
                    child: const Text('Inicia sesión'),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
