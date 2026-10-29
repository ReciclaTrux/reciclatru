import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_spacing.dart';

/// Motivo del error de carga: sin conexión o falla del servidor.
enum RtErrorKind { noConnection, serverError }

/// Pantalla de error de carga con Reintentar.
///
/// Si ya había datos en pantalla, no la reemplaces: usa `RtSnackbar`.
class RtErrorState extends StatelessWidget {
  const RtErrorState({
    super.key,
    required this.title,
    required this.message,
    required this.onRetry,
    this.kind = RtErrorKind.noConnection,
  });

  final String title;
  final String message;
  final VoidCallback onRetry;
  final RtErrorKind kind;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: RtSpacing.s5,
        vertical: RtSpacing.s7,
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Container(
            width: RtSize.stateIcon,
            height: RtSize.stateIcon,
            decoration: BoxDecoration(
              color: colors.errorContainer,
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Icon(
              kind == RtErrorKind.noConnection
                  ? Icons.wifi_off_rounded
                  : Icons.cloud_off_rounded,
              size: RtSize.stateIconGlyph,
              color: colors.onErrorContainer,
            ),
          ),
          const SizedBox(height: RtSpacing.s2),
          Text(
            title,
            textAlign: TextAlign.center,
            style: theme.textTheme.titleMedium!.copyWith(
              color: colors.onSurface,
            ),
          ),
          const SizedBox(height: RtSpacing.s2),
          ConstrainedBox(
            constraints: const BoxConstraints(
              maxWidth: RtSize.stateMessageMaxWidth,
            ),
            child: Text(
              message,
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyLarge!.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
          ),
          const SizedBox(height: RtSpacing.s3),
          RtButton(
            text: 'Reintentar',
            onClick: onRetry,
            variant: RtButtonVariant.secondary,
            icon: Icons.refresh_rounded,
          ),
        ],
      ),
    );
  }
}
