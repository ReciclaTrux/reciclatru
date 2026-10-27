import 'package:flutter/material.dart';
import 'package:reciclatru/core/design_system/atoms/rt_avatar.dart';
import 'package:reciclatru/core/design_system/atoms/rt_badge.dart';
import 'package:reciclatru/core/design_system/atoms/rt_button.dart';
import 'package:reciclatru/core/design_system/atoms/rt_icon_button.dart';
import 'package:reciclatru/core/design_system/atoms/rt_spinner.dart';
import 'package:reciclatru/core/design_system/atoms/rt_text_field.dart';
import 'package:reciclatru/core/design_system/theme/rt_colors.dart';
import 'package:reciclatru/core/design_system/theme/rt_spacing.dart';
import 'package:reciclatru/core/design_system/theme/rt_theme.dart';
import 'package:widgetbook/widgetbook.dart';

void main() {
  runApp(const DesignSystemApp());
}

/// Catálogo del sistema de diseño de ReciclaTru.
///
/// Se levanta con `flutter run -t widgetbook/main.dart`.
class DesignSystemApp extends StatelessWidget {
  const DesignSystemApp({super.key});

  @override
  Widget build(BuildContext context) {
    return Widgetbook.material(
      directories: _directories,
      lightTheme: rtTheme,
      darkTheme: rtTheme,
      themeMode: ThemeMode.light,
    );
  }
}

final List<WidgetbookNode> _directories = <WidgetbookNode>[
  WidgetbookFolder(
    name: 'Átomos',
    isInitiallyExpanded: true,
    children: <WidgetbookNode>[
      WidgetbookComponent(
        name: 'RtButton',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Variantes',
            builder: (_) => _preview(const _ButtonVariants()),
          ),
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _ButtonStates()),
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtTextField',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Variantes',
            builder: (_) => _preview(const _TextFieldVariants()),
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtSpinner',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Tamaños',
            builder: (_) => _preview(const _SpinnerSizes()),
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtIconButton',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Variantes',
            builder: (_) => _preview(const _IconButtonVariants()),
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtAvatar',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Iniciales',
            builder: (_) => _preview(const _AvatarInitials()),
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtBadge',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Variantes',
            builder: (_) => _preview(const _BadgeVariants()),
          ),
        ],
      ),
    ],
  ),
];

Widget _preview(Widget child) {
  return Container(
    color: rtBackground,
    alignment: Alignment.topLeft,
    padding: const EdgeInsets.all(RtSpacing.s4),
    child: child,
  );
}

class _ButtonVariants extends StatelessWidget {
  const _ButtonVariants();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Wrap(
          spacing: RtSpacing.s3,
          runSpacing: RtSpacing.s3,
          crossAxisAlignment: WrapCrossAlignment.center,
          children: <Widget>[
            RtButton(text: 'Publicar pedido', onClick: () {}),
            RtButton(
              text: 'Reintentar',
              variant: RtButtonVariant.secondary,
              onClick: () {},
            ),
            RtButton(
              text: 'Agregar ítem',
              variant: RtButtonVariant.text,
              onClick: () {},
            ),
            RtButton(
              text: 'Sí, cancelar pedido',
              variant: RtButtonVariant.danger,
              onClick: () {},
            ),
            RtButton(text: 'Llamar', icon: Icons.call_rounded, onClick: () {}),
            RtButton(
              text: 'Elegir',
              size: RtButtonSize.compact,
              onClick: () {},
            ),
          ],
        ),
        const SizedBox(height: RtSpacing.s4),
        RtButton(text: 'Publicar pedido', fullWidth: true, onClick: () {}),
      ],
    );
  }
}

class _ButtonStates extends StatelessWidget {
  const _ButtonStates();

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: RtSpacing.s3,
      runSpacing: RtSpacing.s3,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: <Widget>[
        RtButton(text: 'Publicando…', loading: true, onClick: () {}),
        RtButton(
          text: 'Ya aceptaste este pedido',
          icon: Icons.check_rounded,
          enabled: false,
          onClick: () {},
        ),
        RtButton(
          text: 'Reintentar',
          variant: RtButtonVariant.secondary,
          enabled: false,
          onClick: () {},
        ),
      ],
    );
  }
}

class _TextFieldVariants extends StatefulWidget {
  const _TextFieldVariants();

  @override
  State<_TextFieldVariants> createState() => _TextFieldVariantsState();
}

class _TextFieldVariantsState extends State<_TextFieldVariants> {
  String _email = 'juan.perez@gmail.com';
  String _password = 'recojo2026';
  String _phone = '94812';
  String _weight = '2.5';
  String _reference = 'Dpto. 302, puerta negra, frente al parque';

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtTextField(
          label: 'Correo electrónico',
          value: _email,
          onValueChange: (value) => setState(() => _email = value),
          leadingIcon: Icons.mail_rounded,
          keyboardType: TextInputType.emailAddress,
          error: 'Ya existe una cuenta con este correo',
        ),
        const SizedBox(height: RtSpacing.s3),
        RtTextField(
          label: 'Contraseña',
          value: _password,
          onValueChange: (value) => setState(() => _password = value),
          leadingIcon: Icons.lock_rounded,
          helper: 'Mínimo 8 caracteres',
          obscureText: true,
          keyboardType: TextInputType.visiblePassword,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtTextField(
          label: 'Celular',
          value: _phone,
          onValueChange: (value) => setState(() => _phone = value),
          leadingIcon: Icons.phone_iphone_rounded,
          keyboardType: TextInputType.phone,
          error: 'Debe tener 9 dígitos y empezar con 9',
        ),
        const SizedBox(height: RtSpacing.s3),
        RtTextField(
          label: 'Peso',
          value: _weight,
          onValueChange: (value) => setState(() => _weight = value),
          suffix: 'kg',
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
        ),
        const SizedBox(height: RtSpacing.s3),
        RtTextField(
          label: 'Referencia (opcional)',
          value: _reference,
          onValueChange: (value) => setState(() => _reference = value),
          placeholder: 'Dpto. 302, puerta negra, frente al parque',
          helper: 'Ayuda al reciclador a encontrar tu puerta',
          maxLength: 150,
          maxLines: 2,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtTextField(
          label: 'Rol',
          value: 'Vecino',
          onValueChange: (_) {},
          readOnly: true,
        ),
      ],
    );
  }
}

class _SpinnerSizes extends StatelessWidget {
  const _SpinnerSizes();

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Wrap(
      spacing: RtSpacing.s3,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: <Widget>[
        const RtSpinner(size: RtSpinnerSize.large),
        RtSpinner(color: colors.primary),
        const Text('Cargando…'),
        const RtSpinner(),
      ],
    );
  }
}

class _IconButtonVariants extends StatelessWidget {
  const _IconButtonVariants();

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: RtSpacing.s3,
      runSpacing: RtSpacing.s3,
      children: <Widget>[
        RtIconButton(
          icon: Icons.arrow_back_rounded,
          contentDescription: 'Volver',
          variant: RtIconButtonVariant.ink,
          onClick: () {},
        ),
        RtIconButton(
          icon: Icons.visibility_rounded,
          contentDescription: 'Mostrar contraseña',
          onClick: () {},
        ),
        RtIconButton(
          icon: Icons.delete_rounded,
          contentDescription: 'Quitar ítem',
          onClick: () {},
        ),
        RtIconButton(
          icon: Icons.content_copy_rounded,
          contentDescription: 'Copiar dirección',
          onClick: () {},
        ),
        RtIconButton(
          icon: Icons.call_rounded,
          contentDescription: 'Llamar',
          variant: RtIconButtonVariant.tonal,
          onClick: () {},
        ),
        RtIconButton(
          icon: Icons.directions_rounded,
          contentDescription: 'Cómo llegar',
          variant: RtIconButtonVariant.tonal,
          onClick: () {},
        ),
      ],
    );
  }
}

class _AvatarInitials extends StatelessWidget {
  const _AvatarInitials();

  @override
  Widget build(BuildContext context) {
    return const Wrap(
      spacing: RtSpacing.s3,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: <Widget>[
        RtAvatar(name: 'Juan Pérez'),
        RtAvatar(name: 'María Ramos'),
        RtAvatar(name: 'Ana'),
      ],
    );
  }
}

class _BadgeVariants extends StatelessWidget {
  const _BadgeVariants();

  @override
  Widget build(BuildContext context) {
    return const Wrap(
      spacing: RtSpacing.s3,
      runSpacing: RtSpacing.s3,
      crossAxisAlignment: WrapCrossAlignment.center,
      children: <Widget>[
        RtBadge(text: 'Ya aceptaste', icon: Icons.check_rounded),
        RtBadge(
          text: '3 aceptaciones',
          icon: Icons.group_rounded,
          variant: RtBadgeVariant.neutral,
        ),
        RtBadge(text: 'Sin aceptaciones aún', variant: RtBadgeVariant.neutral),
        RtBadge(text: '3', variant: RtBadgeVariant.count),
      ],
    );
  }
}
