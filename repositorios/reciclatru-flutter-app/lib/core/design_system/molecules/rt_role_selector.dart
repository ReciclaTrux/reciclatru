import 'package:flutter/material.dart';

import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Rol que se elige al registrarse (HU-01).
enum RtRole { neighbor, recycler }

/// Dos opciones grandes para elegir el rol al registrarse.
///
/// Arranca con [selected] en `null`: la persona debe elegir a propósito.
class RtRoleSelector extends StatelessWidget {
  const RtRoleSelector({
    super.key,
    required this.selected,
    required this.onSelect,
    this.error,
  });

  final RtRole? selected;
  final ValueChanged<RtRole> onSelect;

  /// Si existe, pinta el borde de ambas opciones en `error` y muestra el mensaje.
  final String? error;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final helpStyle = theme.textTheme.labelMedium!.copyWith(
      color: colors.onSurfaceVariant,
    );
    final hasError = error != null && error!.isNotEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Text(
          '¿Cómo usarás ReciclaTru?',
          style: theme.textTheme.labelLarge!.copyWith(color: colors.onSurface),
        ),
        const SizedBox(height: RtSpacing.s3),
        _RtRoleOption(
          icon: Icons.home_rounded,
          title: 'Soy vecino',
          description: 'Publico el material que quiero entregar',
          selected: selected == RtRole.neighbor,
          hasError: hasError,
          onTap: () => onSelect(RtRole.neighbor),
        ),
        const SizedBox(height: RtSpacing.s3),
        _RtRoleOption(
          icon: Icons.recycling_rounded,
          title: 'Soy reciclador',
          description: 'Recojo material cerca de mí',
          selected: selected == RtRole.recycler,
          hasError: hasError,
          onTap: () => onSelect(RtRole.recycler),
        ),
        const SizedBox(height: RtSpacing.s3),
        Row(
          children: <Widget>[
            Icon(
              Icons.info_rounded,
              size: RtSize.iconXs,
              color: colors.onSurfaceVariant,
            ),
            const SizedBox(width: RtSpacing.s1),
            Expanded(
              child: Text('No podrás cambiar el rol después', style: helpStyle),
            ),
          ],
        ),
        if (hasError) ...<Widget>[
          const SizedBox(height: RtSpacing.s1),
          Row(
            children: <Widget>[
              Icon(
                Icons.error_rounded,
                size: RtSize.iconXs,
                color: colors.error,
              ),
              const SizedBox(width: RtSpacing.s1),
              Expanded(
                child: Text(
                  error!,
                  style: helpStyle.copyWith(color: colors.error),
                ),
              ),
            ],
          ),
        ],
      ],
    );
  }
}

class _RtRoleOption extends StatelessWidget {
  const _RtRoleOption({
    required this.icon,
    required this.title,
    required this.description,
    required this.selected,
    required this.hasError,
    required this.onTap,
  });

  final IconData icon;
  final String title;
  final String description;
  final bool selected;
  final bool hasError;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final borderColor = hasError
        ? colors.error
        : selected
        ? colors.primary
        : colors.outline;
    final borderWidth = selected ? RtStroke.roleSelected : RtStroke.control;
    final shape = RoundedRectangleBorder(
      borderRadius: BorderRadius.circular(RtRadii.md),
      side: BorderSide(color: borderColor, width: borderWidth),
    );

    return Semantics(
      selected: selected,
      inMutuallyExclusiveGroup: true,
      button: true,
      label: '$title. $description',
      child: Material(
        color: selected ? colors.primaryContainer : colors.surface,
        shape: shape,
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: onTap,
          child: Padding(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: Row(
              children: <Widget>[
                Container(
                  width: RtSize.touchTarget,
                  height: RtSize.touchTarget,
                  decoration: BoxDecoration(
                    color: selected
                        ? colors.primary
                        : colors.surfaceContainerHighest,
                    shape: BoxShape.circle,
                  ),
                  alignment: Alignment.center,
                  child: Icon(
                    icon,
                    size: RtSize.iconMd,
                    color: selected
                        ? colors.onPrimary
                        : colors.onSurfaceVariant,
                  ),
                ),
                const SizedBox(width: RtSpacing.s3),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: <Widget>[
                      Text(
                        title,
                        style: theme.textTheme.bodyLarge!.copyWith(
                          fontWeight: FontWeight.w600,
                          color: colors.onSurface,
                        ),
                      ),
                      Text(
                        description,
                        style: theme.textTheme.bodyMedium!.copyWith(
                          color: colors.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: RtSpacing.s3),
                Container(
                  width: RtSize.roleRadio,
                  height: RtSize.roleRadio,
                  decoration: BoxDecoration(
                    color: selected ? colors.surface : Colors.transparent,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: selected ? colors.primary : colors.outline,
                      width: selected
                          ? RtStroke.roleRadioSelected
                          : RtStroke.roleRadio,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
