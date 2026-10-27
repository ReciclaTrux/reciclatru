import 'package:flutter/material.dart';

import '../atoms/rt_icon_button.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Acción única de la derecha de [RtTopAppBar].
class RtAppBarAction {
  const RtAppBarAction({
    required this.icon,
    required this.contentDescription,
    required this.onClick,
  });

  final IconData icon;
  final String contentDescription;
  final VoidCallback onClick;
}

/// Barra superior con título y, en pantallas internas, botón de volver y una acción.
class RtTopAppBar extends StatelessWidget {
  const RtTopAppBar({super.key, required this.title, this.onBack, this.action});

  final String title;

  /// Si existe, muestra la flecha de volver.
  final VoidCallback? onBack;
  final RtAppBarAction? action;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Container(
          height: RtSize.appbarHeight,
          color: colors.surface,
          padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s1),
          child: Row(
            children: <Widget>[
              if (onBack != null) ...<Widget>[
                RtIconButton(
                  icon: Icons.arrow_back_rounded,
                  contentDescription: 'Volver',
                  variant: RtIconButtonVariant.ink,
                  onClick: onBack!,
                ),
                const SizedBox(width: RtSpacing.s1),
              ],
              Expanded(
                child: Padding(
                  padding: EdgeInsets.only(
                    left: onBack == null ? RtSpacing.s3 : 0,
                  ),
                  child: Text(
                    title,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: theme.textTheme.titleLarge!.copyWith(
                      color: colors.onSurface,
                    ),
                  ),
                ),
              ),
              if (action != null) ...<Widget>[
                const SizedBox(width: RtSpacing.s1),
                RtIconButton(
                  icon: action!.icon,
                  contentDescription: action!.contentDescription,
                  variant: RtIconButtonVariant.ink,
                  onClick: action!.onClick,
                ),
              ],
            ],
          ),
        ),
        Container(height: RtStroke.divider, color: colors.outlineVariant),
      ],
    );
  }
}
