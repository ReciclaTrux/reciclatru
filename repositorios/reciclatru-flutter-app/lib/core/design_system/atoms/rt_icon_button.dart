import 'package:flutter/material.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_spacing.dart';

/// Variante de [RtIconButton].
enum RtIconButtonVariant {
  /// Acciones secundarias repetidas: volver, mostrar u ocultar, quitar.
  standard,

  /// Acciones de contacto: llamar, cómo llegar.
  tonal,

  /// Ícono sobre superficies de marca.
  ink,
}

/// Botón circular de 48 px que solo lleva un ícono.
///
/// Siempre necesita [contentDescription]: es la descripción accesible y el
/// texto del tooltip.
class RtIconButton extends StatelessWidget {
  const RtIconButton({
    super.key,
    required this.icon,
    required this.contentDescription,
    this.onClick,
    this.variant = RtIconButtonVariant.standard,
  });

  final IconData icon;
  final String contentDescription;
  final VoidCallback? onClick;
  final RtIconButtonVariant variant;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final extra = Theme.of(context).extension<RtExtraColors>()!;

    final (background, foreground) = switch (variant) {
      RtIconButtonVariant.standard => (
        Colors.transparent,
        colors.onSurfaceVariant,
      ),
      RtIconButtonVariant.tonal => (
        colors.secondaryContainer,
        colors.onSecondaryContainer,
      ),
      RtIconButtonVariant.ink => (Colors.transparent, colors.onSurface),
    };

    return IconButton(
      onPressed: onClick,
      tooltip: contentDescription,
      icon: Icon(icon),
      iconSize: RtSize.iconMd,
      style: IconButton.styleFrom(
        backgroundColor: background,
        foregroundColor: foreground,
        disabledBackgroundColor: variant == RtIconButtonVariant.tonal
            ? extra.disabledBg
            : Colors.transparent,
        disabledForegroundColor: extra.disabledInk,
        minimumSize: const Size(RtSize.touchTarget, RtSize.touchTarget),
        fixedSize: const Size(RtSize.touchTarget, RtSize.touchTarget),
        padding: EdgeInsets.zero,
        shape: const CircleBorder(),
        tapTargetSize: MaterialTapTargetSize.padded,
      ),
    );
  }
}
