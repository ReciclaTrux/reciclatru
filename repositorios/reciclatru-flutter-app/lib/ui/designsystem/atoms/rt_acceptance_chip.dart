/// Define el componente reutilizable rt acceptance chip del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Estado visible de la aceptación de un reciclador.
enum RtAcceptanceState { waiting, chosen }

/// Chip accesible que distingue una aceptación pendiente de la selección final.
class RtAcceptanceChip extends StatelessWidget {
  const RtAcceptanceChip({super.key, required this.state});
  final RtAcceptanceState state;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final chosen = state == RtAcceptanceState.chosen;
    return Container(
      constraints: const BoxConstraints(minHeight: RtSize.touchTarget),
      padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s3),
      decoration: BoxDecoration(
        color: chosen ? colors.secondaryContainer : colors.primaryContainer,
        borderRadius: BorderRadius.circular(RtRadii.full),
      ),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Icon(chosen ? Icons.check_circle_rounded : Icons.schedule_rounded,
            size: RtSize.iconXs,
            color: chosen ? colors.onSecondaryContainer : colors.onPrimaryContainer),
        const SizedBox(width: RtSpacing.s1),
        Text(chosen ? 'Te eligió' : 'En espera',
            style: Theme.of(context).textTheme.labelMedium?.copyWith(
              color: chosen ? colors.onSecondaryContainer : colors.onPrimaryContainer,
            )),
      ]),
    );
  }
}
