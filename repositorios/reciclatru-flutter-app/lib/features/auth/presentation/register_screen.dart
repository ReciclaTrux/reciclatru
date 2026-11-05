
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
          RegExp(r'^[\w.+-]+@([\w-]+\.)+[\w-]{2,}$');

      if (!emailRegex.hasMatch(email)) {
        _emailError = 'Ingresa un correo electrónico válido.';
        hasError = true;
      }
    }

    // Validar contraseña
    if (_password.isEmpty) {
      _passwordError = 'La contraseña es obligatoria.';
      hasError = true;
    } else if (_password.length < 8) {
      _passwordError =
          'La contraseña debe tener al menos 8 caracteres.';
      hasError = true;
    }

    // Validar selección de rol
    if (_selectedRole == null) {
      _roleError = 'Selecciona cómo utilizarás ReciclaTru.';
      hasError = true;
    }

    setState(() {});

    if (hasError) return;

    // Convertir el enum al valor esperado por el backend
    final role = _selectedRole == RtRole.neighbor
        ? 'NEIGHBOR'
        : 'RECYCLER';

    // Activar carga
    ref.read(authLoadingProvider.notifier).isLoading = true;

    try {
      final authRepo = ref.read(authRepositoryProvider);

      // Conectar con el backend
      final user = await authRepo.register(
        email,
        _password,
        role,
      );

      if (!mounted) return;

      // Mostrar confirmación
      showRtSnackbar(
        context,
        message: '¡Cuenta creada correctamente para ${user.email}!',
      );

      // Regresar a la pantalla anterior
      Navigator.pop(context);
    } catch (e) {
      if (!mounted) return;

      setState(() {
        _formError = e
            .toString()
            .replaceFirst('Exception: ', '');
      });
    } finally {
      if (mounted) {
        ref.read(authLoadingProvider.notifier).isLoading = false;
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final isLoading = ref.watch(authLoadingProvider);
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
                'Únete a ReciclaTru',
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

              // Información adicional
              Text(
                'Al crear tu cuenta podrás participar en '
                'la comunidad de reciclaje de ReciclaTru.',
                textAlign: TextAlign.center,
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
