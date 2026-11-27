/// Define el componente reutilizable rt action bar del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_elevation.dart';

/// Barra fija al pie con la acción principal y, como mucho, una secundaria.
///
/// Pasa los botones con `fullWidth: true` para que ocupen su mitad de la barra.
class RtActionBar extends StatelessWidget {
  const RtActionBar({super.key, required this.primary, this.secondary});

  /// La acción principal, a la derecha.
  final Widget primary;

  /// La acción secundaria, a la izquierda.
  final Widget? secondary;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.viewInsetsOf(context).bottom),
      child: Container(
        decoration: BoxDecoration(
          color: colors.surface,
          boxShadow: RtElevation.level2,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            Container(height: RtStroke.divider, color: colors.outlineVariant),
            Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: RtSpacing.s4,
                vertical: RtSpacing.s3,
              ),
              child: Row(
                children: <Widget>[
                  if (secondary != null) ...<Widget>[
                    Expanded(child: secondary!),
                    const SizedBox(width: RtSpacing.s3),
                  ],
                  Expanded(child: primary),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
