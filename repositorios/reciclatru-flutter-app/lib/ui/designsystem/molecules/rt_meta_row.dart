/// Define el componente reutilizable rt meta row del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Dato compacto con ícono usado en tarjetas y detalles.
class RtMeta {
  const RtMeta(this.icon, this.label);
  final IconData icon;
  final String label;
}

/// Alinea varios datos secundarios en una fila adaptable.
class RtMetaRow extends StatelessWidget {
  const RtMetaRow({super.key, required this.items});
  final List<RtMeta> items;

  @override
  Widget build(BuildContext context) => Wrap(
        spacing: RtSpacing.s3,
        runSpacing: RtSpacing.s1,
        children: items.map((item) => Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(item.icon, size: RtSize.iconXs, color: Theme.of(context).colorScheme.onSurfaceVariant),
            const SizedBox(width: RtSpacing.s1),
            Text(item.label, style: Theme.of(context).textTheme.bodyMedium),
          ],
        )).toList(),
      );
}
