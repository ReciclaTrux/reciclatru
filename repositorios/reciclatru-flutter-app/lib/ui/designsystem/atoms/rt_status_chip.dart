/// Define el componente reutilizable rt status chip del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

/// Estados que presenta la etiqueta de pedido equivalente a la de Kotlin.
enum RtOrderStatus { pending, confirmed, collected, cancelled }

/// Etiqueta de estado con texto e ícono, sin depender solo del color.
class RtStatusChip extends StatelessWidget {
  const RtStatusChip({required this.status, super.key});
  final RtOrderStatus status;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final (label, icon, background, foreground) = switch (status) {
      RtOrderStatus.pending => ('Pendiente', Icons.schedule_rounded, colors.tertiaryContainer, colors.onTertiaryContainer),
      RtOrderStatus.confirmed => ('Confirmado', Icons.how_to_reg_rounded, colors.primaryContainer, colors.onPrimaryContainer),
      RtOrderStatus.collected => ('Recolectado', Icons.recycling_rounded, colors.secondaryContainer, colors.onSecondaryContainer),
      RtOrderStatus.cancelled => ('Cancelado', Icons.block_rounded, colors.errorContainer, colors.onErrorContainer),
    };
    return Chip(
      avatar: Icon(icon, size: 16, color: foreground),
      label: Text(label, maxLines: 1),
      backgroundColor: background,
      labelStyle: TextStyle(color: foreground),
      visualDensity: VisualDensity.compact,
    );
  }
}
