import 'package:flutter/material.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_typography.dart';
import 'rt_spinner.dart';

/// Variante visual de [RtButton].
enum RtButtonVariant {
  /// La acción por la que existe la pantalla. Una sola por vista.
  primary,

  /// Alternativa al primario o acción de recuperación.
  secondary,

  /// Acciones menores dentro de un formulario o diálogo.
  text,

  /// Solo para confirmar algo destructivo en un diálogo.
  danger,
}

/// Tamaño de [RtButton].
enum RtButtonSize {
  /// 52 px de alto.
  normal,

  /// 40 px de alto para dentro de tarjetas; su área táctil sigue siendo 48.
  compact,
}

/// Botón de acción con texto.
///
/// Mientras [loading] está activo muestra [RtSpinner] y no acepta toques; el
/// texto debe ir en gerundio con puntos suspensivos ("Publicando…").
class RtButton extends StatelessWidget {
  const RtButton({
    super.key,
    required this.text,
    this.onClick,
    this.variant = RtButtonVariant.primary,
    this.icon,
    this.loading = false,
    this.enabled = true,
    this.fullWidth = false,
    this.size = RtButtonSize.normal,
  });

  final String text;
  final VoidCallback? onClick;
  final RtButtonVariant variant;
  final IconData? icon;
  final bool loading;
  final bool enabled;
  final bool fullWidth;
  final RtButtonSize size;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final extra = Theme.of(context).extension<RtExtraColors>()!;
    final isCompact = size == RtButtonSize.compact;
    final height = isCompact ? RtSize.buttonHeightSm : RtSize.buttonHeight;
    final isActive = enabled && !loading && onClick != null;

    final (background, foreground) = switch (variant) {
      RtButtonVariant.primary => (colors.primary, colors.onPrimary),
      RtButtonVariant.secondary => (colors.surface, colors.primary),
      RtButtonVariant.text => (Colors.transparent, colors.primary),
      RtButtonVariant.danger => (colors.error, colors.onError),
    };
    final contentColor = isActive || loading ? foreground : extra.disabledInk;

    final horizontalPadding = isCompact
        ? RtSpacing.s4
        : switch (variant) {
            RtButtonVariant.text => RtSpacing.s3,
            _ => RtSpacing.s5,
          };

    final shape = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(RtRadii.md),
    );

    final style = ButtonStyle(
      backgroundColor: WidgetStateProperty.resolveWith<Color?>((states) {
        if (states.contains(WidgetState.disabled) && !loading) {
          return extra.disabledBg;
        }
        return background;
      }),
      foregroundColor: WidgetStateProperty.resolveWith<Color?>((states) {
        if (states.contains(WidgetState.disabled) && !loading) {
          return extra.disabledInk;
        }
        return foreground;
      }),
      overlayColor: WidgetStateProperty.resolveWith<Color?>((states) {
        if (states.contains(WidgetState.pressed) ||
            states.contains(WidgetState.focused) ||
            states.contains(WidgetState.hovered)) {
          return foreground.withValues(alpha: 0.12);
        }
        return null;
      }),
      elevation: const WidgetStatePropertyAll<double>(0),
      shadowColor: const WidgetStatePropertyAll<Color>(Colors.transparent),
      minimumSize: WidgetStatePropertyAll<Size>(Size(0, height)),
      padding: WidgetStatePropertyAll<EdgeInsetsGeometry>(
        EdgeInsets.symmetric(horizontal: horizontalPadding),
      ),
      shape: WidgetStatePropertyAll<OutlinedBorder>(shape),
      side: WidgetStateProperty.resolveWith<BorderSide?>((states) {
        if (states.contains(WidgetState.disabled) && !loading) {
          return BorderSide.none;
        }
        if (states.contains(WidgetState.focused)) {
          return BorderSide(color: extra.primaryPressed, width: RtStroke.focus);
        }
        if (variant == RtButtonVariant.secondary) {
          return BorderSide(color: colors.primary, width: RtStroke.control);
        }
        return BorderSide.none;
      }),
      textStyle: WidgetStatePropertyAll<TextStyle>(RtTypography.button),
      iconColor: WidgetStatePropertyAll<Color>(contentColor),
      iconSize: const WidgetStatePropertyAll<double>(RtSize.iconMd),
      tapTargetSize: MaterialTapTargetSize.padded,
      visualDensity: VisualDensity.standard,
      alignment: Alignment.center,
    );

    final content = Row(
      mainAxisSize: fullWidth ? MainAxisSize.max : MainAxisSize.min,
      mainAxisAlignment: MainAxisAlignment.center,
      children: <Widget>[
        if (loading) ...<Widget>[
          RtSpinner(color: contentColor),
          const SizedBox(width: RtSpacing.s2),
        ] else if (icon != null) ...<Widget>[
          Icon(icon, size: RtSize.iconMd, color: contentColor),
          const SizedBox(width: RtSpacing.s2),
        ],
        Flexible(child: Text(text, textAlign: TextAlign.center)),
      ],
    );

    final onPressed = isActive ? onClick : null;
    final button = switch (variant) {
      RtButtonVariant.primary || RtButtonVariant.danger => FilledButton(
        style: style,
        onPressed: onPressed,
        child: content,
      ),
      RtButtonVariant.secondary => OutlinedButton(
        style: style,
        onPressed: onPressed,
        child: content,
      ),
      RtButtonVariant.text => TextButton(
        style: style,
        onPressed: onPressed,
        child: content,
      ),
    };

    if (!fullWidth) {
      return button;
    }
    return SizedBox(width: double.infinity, child: button);
  }
}
