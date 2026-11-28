/// Define el componente reutilizable rt empty state del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Estado vacío de una lista con explicación y acción opcional.
class RtEmptyState extends StatelessWidget {
  const RtEmptyState({
    super.key,
    required this.icon,
    required this.title,
    required this.message,
    this.actionLabel,
    this.actionIcon,
    this.onAction,
  });

  final IconData icon;
  final String title;
  final String message;
  final String? actionLabel;
  final IconData? actionIcon;
  final VoidCallback? onAction;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s5, vertical: RtSpacing.s7),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          CircleAvatar(
            radius: 36,
            backgroundColor: colors.primaryContainer,
            child: Icon(icon, size: 36, color: colors.onPrimaryContainer),
          ),
          const SizedBox(height: RtSpacing.s4),
          Text(title, textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: RtSpacing.s2),
          Text(message, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodyLarge),
          if (actionLabel != null && onAction != null) ...[
            const SizedBox(height: RtSpacing.s4),
            RtButton(text: actionLabel!, onClick: onAction!, icon: actionIcon),
          ],
        ],
      ),
    );
  }
}
