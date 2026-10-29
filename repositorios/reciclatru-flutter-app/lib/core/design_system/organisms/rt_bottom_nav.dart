import 'package:flutter/material.dart';

import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Destino de [RtBottomNav].
class RtBottomNavItem {
  const RtBottomNavItem({
    required this.icon,
    required this.label,
    required this.route,
  });

  final IconData icon;
  final String label;

  /// Identificador de la pantalla; se compara con `selected`.
  final String route;
}

/// Navegación principal de tres destinos, distinta para cada rol.
///
/// El destino activo lleva la píldora `primary-container` y el color de tinta
/// principal. Flutter usa el mismo glifo para ambos estados porque su fuente de
/// íconos no tiene la variante de relleno de Material Symbols.
class RtBottomNav extends StatelessWidget {
  const RtBottomNav({
    super.key,
    required this.items,
    required this.selected,
    required this.onSelect,
  });

  final List<RtBottomNavItem> items;

  /// Ruta del destino activo.
  final String selected;
  final ValueChanged<String> onSelect;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Container(height: RtStroke.divider, color: colors.outlineVariant),
        Material(
          color: colors.surface,
          child: SizedBox(
            height: RtSize.bottomnavHeight,
            child: Row(
              children: <Widget>[
                for (final item in items)
                  Expanded(
                    child: _RtNavItem(
                      item: item,
                      selected: item.route == selected,
                      onTap: () => onSelect(item.route),
                      theme: theme,
                    ),
                  ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}

class _RtNavItem extends StatelessWidget {
  const _RtNavItem({
    required this.item,
    required this.selected,
    required this.onTap,
    required this.theme,
  });

  final RtBottomNavItem item;
  final bool selected;
  final VoidCallback onTap;
  final ThemeData theme;

  @override
  Widget build(BuildContext context) {
    final colors = theme.colorScheme;

    return Semantics(
      selected: selected,
      button: true,
      label: item.label,
      child: InkWell(
        onTap: onTap,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            Container(
              width: RtSize.navPillWidth,
              height: RtSize.navPillHeight,
              decoration: BoxDecoration(
                color: selected ? colors.primaryContainer : Colors.transparent,
                borderRadius: BorderRadius.circular(RtRadii.full),
              ),
              alignment: Alignment.center,
              child: Icon(
                item.icon,
                size: RtSize.iconMd,
                color: selected
                    ? colors.onPrimaryContainer
                    : colors.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: RtSpacing.s1),
            Text(
              item.label,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.labelMedium!.copyWith(
                color: selected ? colors.onSurface : colors.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
