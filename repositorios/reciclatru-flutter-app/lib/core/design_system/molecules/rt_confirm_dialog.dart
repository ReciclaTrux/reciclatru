import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Diálogo que pide confirmar una acción importante o que no se puede deshacer.
///
/// Es la tarjeta del diálogo; para mostrarla usa [RtConfirmDialog.show], que
/// además bloquea el cierre mientras [loading] está activo.
class RtConfirmDialog extends StatelessWidget {
  const RtConfirmDialog({
    super.key,
    required this.title,
    required this.message,
    required this.confirmLabel,
    required this.onConfirm,
    required this.onDismiss,
    this.dismissLabel = 'Volver',
    this.destructive = false,
    this.loading = false,
    this.notice,
  });

  final String title;
  final String message;
  final String confirmLabel;
  final VoidCallback onConfirm;
  final VoidCallback onDismiss;
  final String dismissLabel;
  final bool destructive;
  final bool loading;

  /// Aviso opcional entre el mensaje y los botones, por ejemplo un RtInlineAlert.
  final Widget? notice;

  /// Abre el diálogo con el velo y bloquea el cierre mientras [loading].
  static Future<void> show(
    BuildContext context, {
    required String title,
    required String message,
    required String confirmLabel,
    required VoidCallback onConfirm,
    required VoidCallback onDismiss,
    String dismissLabel = 'Volver',
    bool destructive = false,
    bool loading = false,
    Widget? notice,
  }) {
    return showDialog<void>(
      context: context,
      barrierDismissible: !loading,
      builder: (context) {
        return PopScope(
          canPop: !loading,
          child: Dialog(
            backgroundColor: Colors.transparent,
            elevation: 0,
            insetPadding: const EdgeInsets.all(RtSpacing.s4),
            child: RtConfirmDialog(
              title: title,
              message: message,
              confirmLabel: confirmLabel,
              onConfirm: onConfirm,
              onDismiss: onDismiss,
              dismissLabel: dismissLabel,
              destructive: destructive,
              loading: loading,
              notice: notice,
            ),
          ),
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return ConstrainedBox(
      constraints: const BoxConstraints(maxWidth: RtSize.dialogMaxWidth),
      child: Container(
        padding: const EdgeInsets.all(RtSpacing.s5),
        decoration: BoxDecoration(
          color: colors.surface,
          borderRadius: BorderRadius.circular(RtRadii.lg),
          boxShadow: RtElevation.level3,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Text(
              title,
              style: theme.textTheme.titleLarge!.copyWith(
                color: colors.onSurface,
              ),
            ),
            Padding(
              padding: const EdgeInsets.only(
                top: RtSpacing.s2,
                bottom: RtSpacing.s4,
              ),
              child: Text(
                message,
                style: theme.textTheme.bodyLarge!.copyWith(
                  color: colors.onSurfaceVariant,
                ),
              ),
            ),
            if (notice != null)
              Padding(
                padding: const EdgeInsets.only(bottom: RtSpacing.s4),
                child: notice!,
              ),
            Wrap(
              alignment: WrapAlignment.end,
              spacing: RtSpacing.s2,
              runSpacing: RtSpacing.s2,
              children: <Widget>[
                RtButton(
                  text: dismissLabel,
                  onClick: onDismiss,
                  variant: RtButtonVariant.text,
                  enabled: !loading,
                ),
                RtButton(
                  text: confirmLabel,
                  onClick: onConfirm,
                  variant: destructive
                      ? RtButtonVariant.danger
                      : RtButtonVariant.primary,
                  loading: loading,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
