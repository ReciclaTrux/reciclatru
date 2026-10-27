import 'package:flutter/material.dart';

import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';
import '../theme/rt_typography.dart';

/// Mensaje breve en la parte inferior que confirma una acción o avisa un fallo
/// recuperable.
///
/// Para mostrarlo usa [showRtSnackbar], que lo apoya en `ScaffoldMessenger`.
class RtSnackbar extends StatelessWidget {
  const RtSnackbar({
    super.key,
    required this.message,
    this.actionLabel,
    this.onAction,
  });

  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Container(
      constraints: const BoxConstraints(minHeight: RtSize.touchTarget),
      padding: const EdgeInsets.only(
        left: RtSpacing.s4,
        right: RtSpacing.s2,
        top: RtSpacing.s2,
        bottom: RtSpacing.s2,
      ),
      decoration: BoxDecoration(
        color: colors.inverseSurface,
        borderRadius: BorderRadius.circular(RtRadii.md),
        boxShadow: RtElevation.level2,
      ),
      child: Row(
        children: <Widget>[
          Expanded(
            child: Text(
              message,
              style: theme.textTheme.bodyLarge!.copyWith(
                color: colors.onInverseSurface,
              ),
            ),
          ),
          if (actionLabel != null && onAction != null)
            TextButton(
              onPressed: onAction,
              style: TextButton.styleFrom(
                foregroundColor: colors.inversePrimary,
                minimumSize: const Size(0, RtSize.buttonHeightSm),
                padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s3),
              ),
              child: Text(
                actionLabel!,
                maxLines: 1,
                style: RtTypography.button.copyWith(
                  color: colors.inversePrimary,
                ),
              ),
            ),
        ],
      ),
    );
  }
}

/// Muestra [RtSnackbar] con `ScaffoldMessenger`.
///
/// Dura 4 segundos; con acción, 8. La acción también cierra el aviso.
void showRtSnackbar(
  BuildContext context, {
  required String message,
  String? actionLabel,
  VoidCallback? onAction,
}) {
  final messenger = ScaffoldMessenger.of(context);
  messenger.hideCurrentSnackBar();
  messenger.showSnackBar(
    SnackBar(
      content: RtSnackbar(
        message: message,
        actionLabel: actionLabel,
        onAction: actionLabel != null && onAction != null
            ? () {
                messenger.hideCurrentSnackBar();
                onAction();
              }
            : null,
      ),
      backgroundColor: Colors.transparent,
      elevation: 0,
      behavior: SnackBarBehavior.floating,
      padding: EdgeInsets.zero,
      margin: const EdgeInsets.all(RtSpacing.s4),
      duration: Duration(seconds: actionLabel != null ? 8 : 4),
    ),
  );
}
