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
import 'package:maplibre_gl/maplibre_gl.dart';
import 'package:widgetbook/widgetbook.dart';

import 'lote2_cases.dart';

void main() {
  // TextureView en Android: permite que las sugerencias y la hoja del mapa
  // reciban los toques por encima de la vista de plataforma.
  MapLibreMap.useHybridComposition = true;
  runApp(const DesignSystemApp());
}

/// Catálogo del sistema de diseño de ReciclaTru.
///
/// Se levanta con `flutter run -t widgetbook/main.dart`.
class DesignSystemApp extends StatelessWidget {
  const DesignSystemApp({super.key, this.initialRoute = '/'});

  /// Ruta inicial del catálogo; permite abrir un caso directamente.
  final String initialRoute;

  @override
  Widget build(BuildContext context) {
    return Widgetbook.material(
      initialRoute: initialRoute,
      directories: _directories,
      appBuilder: (context, child) => MaterialApp(
        debugShowCheckedModeBanner: false,
        theme: rtTheme,
        home: Material(child: child),
      ),
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildButtonPlayground,
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildTextFieldPlayground,
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildSpinnerPlayground,
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildIconButtonPlayground,
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildAvatarPlayground,
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
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildBadgePlayground,
          ),
        ],
      ),
    ],
  ),
  ...lote2Directories,
];

Widget _preview(Widget child) {
  return ColoredBox(
    color: rtBackground,
    child: SingleChildScrollView(
      padding: const EdgeInsets.all(RtSpacing.s4),
      child: child,
    ),
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
  bool _obscurePassword = true;

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
          obscureText: _obscurePassword,
          keyboardType: TextInputType.visiblePassword,
          trailingIcon: _obscurePassword
              ? Icons.visibility_rounded
              : Icons.visibility_off_rounded,
          trailingIconDescription: _obscurePassword
              ? 'Mostrar contraseña'
              : 'Ocultar contraseña',
          onTrailingIconClick: () {
            setState(() => _obscurePassword = !_obscurePassword);
          },
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
          multiline: true,
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

// ---------------------------------------------------------------------------
// Casos interactivos (knobs)
// ---------------------------------------------------------------------------

Widget _buildButtonPlayground(BuildContext context) {
  final text = context.knobs.string(
    label: 'Texto',
    initialValue: 'Publicar pedido',
  );
  final variant = context.knobs.object.dropdown<RtButtonVariant>(
    label: 'Variante',
    options: RtButtonVariant.values,
    initialOption: RtButtonVariant.primary,
    labelBuilder: _buttonVariantLabel,
  );
  final size = context.knobs.object.dropdown<RtButtonSize>(
    label: 'Tamaño',
    options: RtButtonSize.values,
    initialOption: RtButtonSize.normal,
    labelBuilder: _buttonSizeLabel,
  );
  final iconLabel = context.knobs.object.dropdown<String>(
    label: 'Ícono',
    options: const <String>['Ninguno', 'Check', 'Llamar', 'Reciclar'],
    initialOption: 'Ninguno',
  );
  final loading = context.knobs.boolean(label: 'Cargando', initialValue: false);
  final enabled = context.knobs.boolean(
    label: 'Habilitado',
    initialValue: true,
  );
  final fullWidth = context.knobs.boolean(
    label: 'Ancho completo',
    initialValue: false,
  );

  return _preview(
    _ButtonPlayground(
      text: text,
      variant: variant,
      size: size,
      icon: _iconByLabel(iconLabel),
      loading: loading,
      enabled: enabled,
      fullWidth: fullWidth,
    ),
  );
}

Widget _buildTextFieldPlayground(BuildContext context) {
  final label = context.knobs.string(
    label: 'Etiqueta',
    initialValue: 'Referencia (opcional)',
  );
  final initialValue = context.knobs.string(
    label: 'Valor inicial',
    initialValue: 'Dpto. 302, puerta negra',
  );
  final placeholder = context.knobs.string(
    label: 'Placeholder',
    initialValue: 'Dpto. 302, puerta negra, frente al parque',
  );
  final helper = context.knobs.string(
    label: 'Ayuda',
    initialValue: 'Ayuda al reciclador a encontrar tu puerta',
  );
  final error = context.knobs.string(label: 'Error', initialValue: '');
  final showLeadingIcon = context.knobs.boolean(
    label: 'Ícono inicial',
    initialValue: false,
  );
  final suffix = context.knobs.string(label: 'Sufijo', initialValue: '');
  final maxLength = context.knobs.int.slider(
    label: 'Máx. caracteres',
    initialValue: 150,
    min: 0,
    max: 200,
    divisions: 20,
  );
  final keyboardLabel = context.knobs.object.dropdown<String>(
    label: 'Teclado',
    options: const <String>['Texto', 'Correo', 'Teléfono', 'Decimal'],
    initialOption: 'Texto',
  );
  final readOnly = context.knobs.boolean(
    label: 'Solo lectura',
    initialValue: false,
  );
  final multiline = context.knobs.boolean(
    label: 'Multilínea',
    initialValue: false,
  );
  final obscure = context.knobs.boolean(
    label: 'Ocultar valor',
    initialValue: false,
  );
  final showTrailingIcon = context.knobs.boolean(
    label: 'Ícono final (ojo)',
    initialValue: false,
  );

  return _preview(
    _TextFieldPlayground(
      label: label,
      initialValue: initialValue,
      placeholder: placeholder.isEmpty ? null : placeholder,
      helper: helper.isEmpty ? null : helper,
      error: error.isEmpty ? null : error,
      leadingIcon: showLeadingIcon ? Icons.location_on_rounded : null,
      suffix: suffix.isEmpty ? null : suffix,
      maxLength: maxLength == 0 ? null : maxLength,
      keyboardType: _keyboardTypeByLabel(keyboardLabel),
      readOnly: readOnly,
      multiline: multiline,
      initialObscured: obscure,
      showTrailingIcon: showTrailingIcon,
    ),
  );
}

Widget _buildSpinnerPlayground(BuildContext context) {
  final size = context.knobs.object.dropdown<RtSpinnerSize>(
    label: 'Tamaño',
    options: RtSpinnerSize.values,
    initialOption: RtSpinnerSize.large,
    labelBuilder: _spinnerSizeLabel,
  );
  final color = context.knobs.color(
    label: 'Color',
    initialValue: Theme.of(context).colorScheme.primary,
  );

  return _preview(RtSpinner(size: size, color: color));
}

Widget _buildIconButtonPlayground(BuildContext context) {
  final variant = context.knobs.object.dropdown<RtIconButtonVariant>(
    label: 'Variante',
    options: RtIconButtonVariant.values,
    initialOption: RtIconButtonVariant.standard,
    labelBuilder: _iconButtonVariantLabel,
  );
  final iconLabel = context.knobs.object.dropdown<String>(
    label: 'Ícono',
    options: const <String>[
      'Llamar',
      'Volver',
      'Visibilidad',
      'Eliminar',
      'Cómo llegar',
    ],
    initialOption: 'Llamar',
  );
  final description = context.knobs.string(
    label: 'Descripción accesible',
    initialValue: 'Llamar a Juan Pérez',
  );

  return _preview(
    _IconButtonPlayground(
      icon: _iconByLabel(iconLabel) ?? Icons.call_rounded,
      contentDescription: description,
      variant: variant,
    ),
  );
}

Widget _buildAvatarPlayground(BuildContext context) {
  final name = context.knobs.string(
    label: 'Nombre y apellido',
    initialValue: 'Juan Pérez',
  );

  return _preview(RtAvatar(name: name));
}

Widget _buildBadgePlayground(BuildContext context) {
  final text = context.knobs.string(
    label: 'Texto',
    initialValue: 'Ya aceptaste',
  );
  final variant = context.knobs.object.dropdown<RtBadgeVariant>(
    label: 'Variante',
    options: RtBadgeVariant.values,
    initialOption: RtBadgeVariant.brand,
    labelBuilder: _badgeVariantLabel,
  );
  final iconLabel = context.knobs.object.dropdown<String>(
    label: 'Ícono',
    options: const <String>['Ninguno', 'Check', 'Grupo', 'Casa'],
    initialOption: 'Check',
  );

  return _preview(
    RtBadge(text: text, icon: _iconByLabel(iconLabel), variant: variant),
  );
}

class _ButtonPlayground extends StatefulWidget {
  const _ButtonPlayground({
    required this.text,
    required this.variant,
    required this.size,
    required this.icon,
    required this.loading,
    required this.enabled,
    required this.fullWidth,
  });

  final String text;
  final RtButtonVariant variant;
  final RtButtonSize size;
  final IconData? icon;
  final bool loading;
  final bool enabled;
  final bool fullWidth;

  @override
  State<_ButtonPlayground> createState() => _ButtonPlaygroundState();
}

class _ButtonPlaygroundState extends State<_ButtonPlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        RtButton(
          text: widget.text,
          variant: widget.variant,
          size: widget.size,
          icon: widget.icon,
          loading: widget.loading,
          enabled: widget.enabled,
          fullWidth: widget.fullWidth,
          onClick: () => setState(() => _taps++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text('Toques: $_taps', style: Theme.of(context).textTheme.bodyMedium),
      ],
    );
  }
}

class _TextFieldPlayground extends StatefulWidget {
  const _TextFieldPlayground({
    required this.label,
    required this.initialValue,
    required this.placeholder,
    required this.helper,
    required this.error,
    required this.leadingIcon,
    required this.suffix,
    required this.maxLength,
    required this.keyboardType,
    required this.readOnly,
    required this.multiline,
    required this.initialObscured,
    required this.showTrailingIcon,
  });

  final String label;
  final String initialValue;
  final String? placeholder;
  final String? helper;
  final String? error;
  final IconData? leadingIcon;
  final String? suffix;
  final int? maxLength;
  final TextInputType keyboardType;
  final bool readOnly;
  final bool multiline;
  final bool initialObscured;
  final bool showTrailingIcon;

  @override
  State<_TextFieldPlayground> createState() => _TextFieldPlaygroundState();
}

class _TextFieldPlaygroundState extends State<_TextFieldPlayground> {
  late String _value;
  late bool _obscured;

  @override
  void initState() {
    super.initState();
    _value = widget.initialValue;
    _obscured = widget.initialObscured;
  }

  @override
  void didUpdateWidget(covariant _TextFieldPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialValue != oldWidget.initialValue) {
      _value = widget.initialValue;
    }
    if (widget.initialObscured != oldWidget.initialObscured) {
      _obscured = widget.initialObscured;
    }
    if (widget.maxLength != null && _value.length > widget.maxLength!) {
      _value = _value.substring(0, widget.maxLength!);
    }
  }

  @override
  Widget build(BuildContext context) {
    return RtTextField(
      label: widget.label,
      value: _value,
      onValueChange: (value) => setState(() => _value = value),
      placeholder: widget.placeholder,
      helper: widget.helper,
      error: widget.error,
      leadingIcon: widget.leadingIcon,
      suffix: widget.suffix,
      maxLength: widget.maxLength,
      keyboardType: widget.keyboardType,
      readOnly: widget.readOnly,
      multiline: widget.multiline,
      obscureText: _obscured,
      trailingIcon: widget.showTrailingIcon
          ? (_obscured
                ? Icons.visibility_rounded
                : Icons.visibility_off_rounded)
          : null,
      trailingIconDescription: widget.showTrailingIcon
          ? (_obscured ? 'Mostrar contraseña' : 'Ocultar contraseña')
          : null,
      onTrailingIconClick: widget.showTrailingIcon
          ? () => setState(() => _obscured = !_obscured)
          : null,
    );
  }
}

class _IconButtonPlayground extends StatefulWidget {
  const _IconButtonPlayground({
    required this.icon,
    required this.contentDescription,
    required this.variant,
  });

  final IconData icon;
  final String contentDescription;
  final RtIconButtonVariant variant;

  @override
  State<_IconButtonPlayground> createState() => _IconButtonPlaygroundState();
}

class _IconButtonPlaygroundState extends State<_IconButtonPlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        RtIconButton(
          icon: widget.icon,
          contentDescription: widget.contentDescription,
          variant: widget.variant,
          onClick: () => setState(() => _taps++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text('Toques: $_taps', style: Theme.of(context).textTheme.bodyMedium),
      ],
    );
  }
}

String _buttonVariantLabel(RtButtonVariant variant) => switch (variant) {
  RtButtonVariant.primary => 'Primario',
  RtButtonVariant.secondary => 'Secundario',
  RtButtonVariant.text => 'Texto',
  RtButtonVariant.danger => 'Peligro',
};

String _buttonSizeLabel(RtButtonSize size) => switch (size) {
  RtButtonSize.normal => 'Normal',
  RtButtonSize.compact => 'Compacto',
};

String _spinnerSizeLabel(RtSpinnerSize size) => switch (size) {
  RtSpinnerSize.small => 'Pequeño',
  RtSpinnerSize.large => 'Grande',
};

String _iconButtonVariantLabel(RtIconButtonVariant variant) =>
    switch (variant) {
      RtIconButtonVariant.standard => 'Estándar',
      RtIconButtonVariant.tonal => 'Tonal',
      RtIconButtonVariant.ink => 'Tinta',
    };

String _badgeVariantLabel(RtBadgeVariant variant) => switch (variant) {
  RtBadgeVariant.brand => 'Marca',
  RtBadgeVariant.neutral => 'Neutra',
  RtBadgeVariant.count => 'Contador',
};

IconData? _iconByLabel(String label) => switch (label) {
  'Check' => Icons.check_rounded,
  'Llamar' => Icons.call_rounded,
  'Reciclar' => Icons.recycling_rounded,
  'Volver' => Icons.arrow_back_rounded,
  'Visibilidad' => Icons.visibility_rounded,
  'Eliminar' => Icons.delete_rounded,
  'Cómo llegar' => Icons.directions_rounded,
  'Grupo' => Icons.group_rounded,
  'Casa' => Icons.home_rounded,
  _ => null,
};

TextInputType _keyboardTypeByLabel(String label) => switch (label) {
  'Correo' => TextInputType.emailAddress,
  'Teléfono' => TextInputType.phone,
  'Decimal' => const TextInputType.numberWithOptions(decimal: true),
  _ => TextInputType.text,
};
