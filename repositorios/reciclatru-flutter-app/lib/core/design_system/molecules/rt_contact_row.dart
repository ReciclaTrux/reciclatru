import 'package:flutter/material.dart';

import '../atoms/rt_avatar.dart';
import '../atoms/rt_icon_button.dart';
import '../theme/rt_spacing.dart';

/// Avatar, nombre, un dato secundario y botón de llamada para una persona.
///
/// Solo se muestra cuando el backend manda el teléfono para el rol y estado.
class RtContactRow extends StatelessWidget {
  const RtContactRow({
    super.key,
    required this.name,
    required this.subtitle,
    this.phone,
    this.onCall,
  });

  final String name;

  /// Celular o fecha de aceptación; si es un celular de 9 dígitos se agrupa.
  final String subtitle;

  /// Si existe, muestra el botón de llamada.
  final String? phone;
  final VoidCallback? onCall;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Row(
      children: <Widget>[
        RtAvatar(name: name),
        const SizedBox(width: RtSpacing.s3),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              Text(
                name,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: theme.textTheme.bodyLarge!.copyWith(
                  fontWeight: FontWeight.w600,
                  color: colors.onSurface,
                ),
              ),
              Text(
                _groupedPhone(subtitle),
                style: theme.textTheme.bodyMedium!.copyWith(
                  color: colors.onSurfaceVariant,
                ),
              ),
            ],
          ),
        ),
        if (phone != null) ...<Widget>[
          const SizedBox(width: RtSpacing.s3),
          RtIconButton(
            icon: Icons.call_rounded,
            contentDescription: 'Llamar a $name',
            variant: RtIconButtonVariant.tonal,
            onClick: onCall ?? () {},
          ),
        ],
      ],
    );
  }

  /// Agrupa un celular de nueve dígitos en tres: "987 654 321".
  String _groupedPhone(String value) {
    final digits = value.replaceAll(RegExp(r'[^0-9]'), '');
    final isPhone = digits.length == 9 && !value.contains(RegExp(r'[a-zA-Z]'));
    if (!isPhone) {
      return value;
    }
    return '${digits.substring(0, 3)} ${digits.substring(3, 6)} '
        '${digits.substring(6)}';
  }
}
