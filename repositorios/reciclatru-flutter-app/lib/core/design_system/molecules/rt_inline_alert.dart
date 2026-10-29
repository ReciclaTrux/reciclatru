import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Tono de [RtInlineAlert]: cada uno tiene su fondo, su texto y su ícono.
enum RtInlineAlertTone { error, warning, info, success }

/// Aviso dentro del contenido, con ícono y texto.
///
/// Para avisos que se quedan en pantalla; un aviso pasajero es `RtSnackbar`.
class RtInlineAlert extends StatelessWidget {
  const RtInlineAlert({
    super.key,
    required this.tone,
    required this.message,
    this.title,
    this.actionLabel,
    this.onAction,
  });

  final RtInlineAlertTone tone;
  final String message;

  /// Línea en negrita opcional encima del mensaje.
  final String? title;

  /// Texto de la acción opcional, por ejemplo "Activar ubicación".
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final extra = theme.extension<RtExtraColors>()!;

    final (background, foreground, icon) = switch (tone) {
      RtInlineAlertTone.error => (
        colors.errorContainer,
        colors.onErrorContainer,
        Icons.error_rounded,
      ),
      RtInlineAlertTone.warning => (
        extra.warningContainer,
        extra.onWarningContainer,
        Icons.warning_rounded,
      ),
      RtInlineAlertTone.info => (
        extra.infoContainer,
        extra.onInfoContainer,
        Icons.info_rounded,
      ),
      RtInlineAlertTone.success => (
        colors.secondaryContainer,
        colors.onSecondaryContainer,
        Icons.recycling_rounded,
      ),
    };

    return Semantics(
      container: true,
      liveRegion: tone == RtInlineAlertTone.error,
      child: Container(
        padding: const EdgeInsets.symmetric(
          horizontal: RtSpacing.s4,
          vertical: RtSpacing.s3,
        ),
        decoration: BoxDecoration(
          color: background,
          borderRadius: BorderRadius.circular(RtRadii.md),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.only(top: RtStroke.thin),
              child: Icon(icon, size: RtSize.iconMd, color: foreground),
            ),
            const SizedBox(width: RtSpacing.s3),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  if (title != null)
                    Text(
                      title!,
                      style: theme.textTheme.bodyLarge!.copyWith(
                        fontWeight: FontWeight.w600,
                        color: foreground,
                      ),
                    ),
                  Text(
                    message,
                    style: theme.textTheme.bodyLarge!.copyWith(
                      color: foreground,
                    ),
                  ),
                  if (actionLabel != null && onAction != null)
                    Padding(
                      padding: const EdgeInsets.only(top: RtSpacing.s2),
                      child: RtButton(
                        text: actionLabel!,
                        onClick: onAction!,
                        variant: RtButtonVariant.text,
                        size: RtButtonSize.compact,
                      ),
                    ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
