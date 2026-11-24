import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Explicación previa al permiso de ubicación del reciclador (HU-10).
///
/// Se muestra una sola vez, antes del diálogo del sistema. [onContinue] lanza
/// el permiso (ubicación aproximada); [onSkip] sigue sin él y la lista se
/// ordena por fecha.
class RtLocationPrompt extends StatelessWidget {
  const RtLocationPrompt({
    super.key,
    required this.onContinue,
    required this.onSkip,
  });

  final VoidCallback onContinue;
  final VoidCallback onSkip;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(RtRadii.lg),
        border: Border.all(color: colors.outlineVariant),
        boxShadow: RtElevation.level1,
      ),
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: RtSpacing.s3,
          vertical: RtSpacing.s5,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            Center(
              child: Container(
                width: RtSize.stateIcon,
                height: RtSize.stateIcon,
                decoration: BoxDecoration(
                  color: colors.primaryContainer,
                  shape: BoxShape.circle,
                ),
                alignment: Alignment.center,
                child: Icon(
                  Icons.near_me_rounded,
                  size: RtSize.stateIconGlyph,
                  color: colors.onPrimaryContainer,
                ),
              ),
            ),
            const SizedBox(height: RtSpacing.s2),
            Text(
              'Usamos tu ubicación solo para mostrar distancias',
              textAlign: TextAlign.center,
              style: theme.textTheme.titleMedium!.copyWith(
                color: colors.onSurface,
              ),
            ),
            const SizedBox(height: RtSpacing.s2),
            Text(
              'No la guardamos ni se la mostramos a los vecinos.',
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyLarge!.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: RtSpacing.s3),
            RtButton(text: 'Continuar', onClick: onContinue, fullWidth: true),
            const SizedBox(height: RtSpacing.s1),
            Center(
              child: RtButton(
                text: 'Ahora no',
                variant: RtButtonVariant.text,
                onClick: onSkip,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
