import 'package:flutter/material.dart';

import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Variante de [RtBadge].
enum RtBadgeVariant {
  /// Marca: "Ya aceptaste" (`primary-container`).
  brand,

  /// Información neutra: aceptaciones, "Sin aceptaciones aún".
  neutral,

  /// Contador sobre un ícono o pestaña (relleno `primary`).
  count,
}

/// Marca pequeña de información que no es un estado.
///
/// Para el estado del pedido se usa `RtStatusChip`, no [RtBadge].
class RtBadge extends StatelessWidget {
  const RtBadge({
    super.key,
    required this.text,
    this.icon,
    this.variant = RtBadgeVariant.brand,
  });

  final String text;
  final IconData? icon;
  final RtBadgeVariant variant;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    final (background, foreground) = switch (variant) {
      RtBadgeVariant.brand => (
        colors.primaryContainer,
        colors.onPrimaryContainer,
      ),
      RtBadgeVariant.neutral => (
        colors.surfaceContainerHighest,
        colors.onSurfaceVariant,
      ),
      RtBadgeVariant.count => (colors.primary, colors.onPrimary),
    };

    final textStyle = theme.textTheme.labelMedium!.copyWith(color: foreground);

    return ConstrainedBox(
      constraints: BoxConstraints(
        minHeight: RtSize.badgeHeight,
        minWidth: variant == RtBadgeVariant.count ? RtSize.badgeHeight : 0,
      ),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: background,
          borderRadius: BorderRadius.circular(RtRadii.full),
        ),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s2),
          child: Center(
            widthFactor: 1,
            heightFactor: 1,
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                if (icon != null) ...<Widget>[
                  Icon(icon, size: RtSize.iconXs, color: foreground),
                  const SizedBox(width: RtSpacing.s1),
                ],
                Text(text, style: textStyle),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
