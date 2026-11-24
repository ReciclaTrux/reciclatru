import 'package:flutter/material.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Etiqueta combinada de la aceptación del reciclador y del pedido (HU-14).
///
/// El ViewModel calcula cuál corresponde; la vista solo la dibuja.
enum RtAcceptanceLabel {
  /// Aceptada con el pedido Pendiente.
  waiting('Esperando confirmación', Icons.hourglass_top_rounded),

  /// Elegida con el pedido Confirmado: la única con relleno sólido.
  chosen('Elegido', Icons.verified_rounded),

  /// Descartada, con cualquier estado de pedido.
  notChosen('No elegido', Icons.person_off_rounded),

  /// El vecino canceló el pedido.
  cancelledByNeighbor('Cancelado por el vecino', Icons.block_rounded),

  /// Elegida con el pedido Recolectado.
  collected('Recolectado', Icons.recycling_rounded);

  const RtAcceptanceLabel(this.label, this.icon);

  final String label;
  final IconData icon;
}

/// Etiqueta de color con ícono y palabra para una aceptación (HU-14).
///
/// Mismas reglas que `RtStatusChip`: ícono más palabra, nunca solo color.
class RtAcceptanceChip extends StatelessWidget {
  const RtAcceptanceChip({super.key, required this.label});

  final RtAcceptanceLabel label;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final extra = theme.extension<RtExtraColors>()!;

    final (background, foreground) = switch (label) {
      RtAcceptanceLabel.waiting => (
        extra.statusPendingBg,
        extra.statusPendingInk,
      ),
      RtAcceptanceLabel.chosen => (extra.statusChosenBg, extra.statusChosenInk),
      RtAcceptanceLabel.notChosen || RtAcceptanceLabel.cancelledByNeighbor => (
        extra.statusCancelledBg,
        extra.statusCancelledInk,
      ),
      RtAcceptanceLabel.collected => (
        extra.statusCollectedBg,
        extra.statusCollectedInk,
      ),
    };

    return Semantics(
      label: label.label,
      child: ConstrainedBox(
        constraints: const BoxConstraints(minHeight: RtSize.chipHeight),
        child: DecoratedBox(
          decoration: BoxDecoration(
            color: background,
            borderRadius: BorderRadius.circular(RtRadii.full),
          ),
          child: Padding(
            padding: const EdgeInsets.only(
              left: RtSpacing.s2,
              right: RtSpacing.chipPaddingEnd,
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                ExcludeSemantics(
                  child: Icon(
                    label.icon,
                    size: RtSize.iconXs,
                    color: foreground,
                  ),
                ),
                const SizedBox(width: RtSpacing.s1),
                Flexible(
                  child: Text(
                    label.label,
                    style: theme.textTheme.labelMedium!.copyWith(
                      color: foreground,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
