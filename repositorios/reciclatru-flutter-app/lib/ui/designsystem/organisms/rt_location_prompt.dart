/// Define el componente reutilizable rt location prompt del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Explica el uso de ubicación antes de que el sistema muestre su permiso.
class RtLocationPrompt extends StatelessWidget {
  const RtLocationPrompt({super.key, required this.onContinue, required this.onDismiss});
  final VoidCallback onContinue;
  final VoidCallback onDismiss;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(RtSpacing.s5),
      decoration: BoxDecoration(
        color: colors.surface,
        border: Border.all(color: colors.outlineVariant),
        borderRadius: BorderRadius.circular(RtRadii.lg),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          CircleAvatar(
            radius: 36,
            backgroundColor: colors.primaryContainer,
            child: Icon(Icons.near_me_rounded, size: 36, color: colors.onPrimaryContainer),
          ),
          const SizedBox(height: RtSpacing.s3),
          Text('Usamos tu ubicación solo para mostrar distancias', textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: RtSpacing.s2),
          Text('Así ves primero los pedidos más cercanos. No la guardamos ni la mostramos a los vecinos.', textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodyLarge),
          const SizedBox(height: RtSpacing.s3),
          RtButton(text: 'Continuar', onClick: onContinue, fullWidth: true),
          RtButton(text: 'Ahora no', onClick: onDismiss, variant: RtButtonVariant.text),
        ],
      ),
    );
  }
}
