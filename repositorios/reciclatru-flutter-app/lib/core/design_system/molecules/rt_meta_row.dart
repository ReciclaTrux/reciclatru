import 'package:flutter/material.dart';

import '../theme/rt_spacing.dart';

/// Un dato corto de [RtMetaRow]: su ícono y su texto ya formateado.
class RtMetaItem {
  const RtMetaItem({required this.icon, required this.text});

  final IconData icon;

  /// Texto del sistema: "a 2.4 km", "14.5 kg", "3 ítems", "hace 2 h".
  final String text;
}

/// Fila de datos cortos con ícono: distancia, peso, cantidad de ítems y tiempo.
///
/// Se ajusta a dos líneas si no entra. Sin ubicación del reciclador no se pasa
/// la distancia: no se escribe "0 km".
class RtMetaRow extends StatelessWidget {
  const RtMetaRow({super.key, required this.items});

  final List<RtMetaItem> items;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final style = theme.textTheme.bodyMedium!.copyWith(
      color: colors.onSurfaceVariant,
    );

    return Wrap(
      spacing: RtSpacing.s3,
      runSpacing: RtSpacing.metaRowGap,
      children: <Widget>[
        for (final item in items)
          Row(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              Icon(
                item.icon,
                size: RtSize.iconSm,
                color: colors.onSurfaceVariant,
              ),
              const SizedBox(width: RtSpacing.s1),
              Flexible(
                child: Text(
                  item.text,
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  style: style,
                ),
              ),
            ],
          ),
      ],
    );
  }
}
