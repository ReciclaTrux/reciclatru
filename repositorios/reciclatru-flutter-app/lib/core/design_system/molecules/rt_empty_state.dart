import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_spacing.dart';

/// Pantalla o sección vacía con ícono, título, una línea de explicación y, si
/// aplica, un botón.
///
/// Los textos del Release 1 salen de su guía ("Aún no publicaste pedidos",
/// "No hay pedidos disponibles por ahora"…). Es una invitación, no una
/// disculpa. Dentro de una lista sigue permitiendo deslizar para actualizar.
class RtEmptyState extends StatelessWidget {
  const RtEmptyState({
    super.key,
    required this.icon,
    required this.title,
    required this.message,
    this.actionLabel,
    this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
  final String? actionLabel;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final hasAction = actionLabel != null && onAction != null;

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
              color: colors.primaryContainer,
              shape: BoxShape.circle,
            ),
            alignment: Alignment.center,
            child: Icon(
              icon,
              size: RtSize.stateIconGlyph,
              color: colors.onPrimaryContainer,
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
          if (hasAction) ...<Widget>[
            const SizedBox(height: RtSpacing.s3),
            RtButton(text: actionLabel!, onClick: onAction!),
          ],
        ],
      ),
    );
  }
}
