import 'package:flutter/material.dart';

import '../atoms/rt_badge.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Pestaña de [RtTabs]: su texto y, si aplica, un contador.
class RtTabItem {
  const RtTabItem({required this.text, this.count});

  final String text;

  /// Contador de la pestaña (por ejemplo, pedidos activos). Solo se dibuja si
  /// es mayor a 0.
  final int? count;
}

/// Dos pestañas de igual ancho para separar pedidos activos del historial
/// (HU-06, HU-14).
///
/// Cada pestaña conserva su posición de scroll al cambiar: eso lo resuelve la
/// pantalla, no la molécula.
class RtTabs extends StatelessWidget {
  const RtTabs({
    super.key,
    required this.tabs,
    required this.selectedIndex,
    required this.onSelect,
  });

  final List<RtTabItem> tabs;
  final int selectedIndex;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Material(
      color: colors.surface,
      child: Stack(
        children: <Widget>[
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: Container(
              height: RtStroke.divider,
              color: colors.outlineVariant,
            ),
          ),
          Row(
            children: <Widget>[
              for (var index = 0; index < tabs.length; index++)
                Expanded(
                  child: _RtTab(
                    tab: tabs[index],
                    selected: index == selectedIndex,
                    onTap: () => onSelect(index),
                  ),
                ),
            ],
          ),
        ],
      ),
    );
  }
}

class _RtTab extends StatelessWidget {
  const _RtTab({
    required this.tab,
    required this.selected,
    required this.onTap,
  });

  final RtTabItem tab;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final count = tab.count ?? 0;
    final foreground = selected ? colors.primary : colors.onSurfaceVariant;

    return Semantics(
      button: true,
      selected: selected,
      label: count > 0 ? '${tab.text}, $count' : tab.text,
      child: InkWell(
        onTap: onTap,
        child: Container(
          height: RtSize.touchTarget,
          decoration: BoxDecoration(
            border: Border(
              bottom: BorderSide(
                color: selected ? colors.primary : Colors.transparent,
                width: RtSize.tabIndicator,
              ),
            ),
          ),
          child: Center(
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                Flexible(
                  child: Text(
                    tab.text,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.labelLarge!.copyWith(
                      color: foreground,
                    ),
                  ),
                ),
                if (count > 0) ...<Widget>[
                  const SizedBox(width: RtSpacing.s2),
                  RtBadge(text: '$count', variant: RtBadgeVariant.count),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}
