import 'package:flutter/material.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Estado de un pedido, con su palabra y su ícono.
///
/// La pantalla no escribe ni el texto ni el ícono: salen del enum. Cancelado es
/// gris a propósito (no es un error del usuario).
enum RtOrderStatus {
  /// El vecino publicó el pedido y espera aceptaciones.
  pending('Pendiente', Icons.schedule_rounded),

  /// El vecino eligió a un reciclador.
  confirmed('Confirmado', Icons.how_to_reg_rounded),

  /// El reciclador entregó el material y el vecino lo marcó.
  collected('Recolectado', Icons.recycling_rounded),

  /// El pedido pasó al historial.
  cancelled('Cancelado', Icons.block_rounded);

  const RtOrderStatus(this.label, this.icon);

  final String label;
  final IconData icon;
}

/// Etiqueta de color con ícono y palabra que muestra el estado de un pedido.
///
/// El reciclador no ve estos estados en su lista de aceptados: ve
/// `RtAcceptanceChip`.
class RtStatusChip extends StatelessWidget {
  const RtStatusChip({super.key, required this.status});

  final RtOrderStatus status;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final extra = theme.extension<RtExtraColors>()!;

    final (background, foreground) = switch (status) {
      RtOrderStatus.pending => (extra.statusPendingBg, extra.statusPendingInk),
      RtOrderStatus.confirmed => (
        extra.statusConfirmedBg,
        extra.statusConfirmedInk,
      ),
      RtOrderStatus.collected => (
        extra.statusCollectedBg,
        extra.statusCollectedInk,
      ),
      RtOrderStatus.cancelled => (
        extra.statusCancelledBg,
        extra.statusCancelledInk,
      ),
    };

    return Semantics(
      label: status.label,
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
                    status.icon,
                    size: RtSize.iconXs,
                    color: foreground,
                  ),
                ),
                const SizedBox(width: RtSpacing.s1),
                Text(
                  status.label,
                  style: theme.textTheme.labelMedium!.copyWith(
                    color: foreground,
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
