import 'package:flutter/material.dart';

import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_typography.dart';

/// Círculo con las iniciales de una persona.
///
/// Es decorativo: el nombre siempre aparece escrito al lado (RtContactRow).
/// No hay fotos de perfil en el Release 1.
class RtAvatar extends StatelessWidget {
  const RtAvatar({super.key, required this.name});

  /// Nombre y apellido; se usan la primera letra de cada uno.
  final String name;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return ExcludeSemantics(
      child: Container(
        width: RtSize.avatarMd,
        height: RtSize.avatarMd,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: colors.secondaryContainer,
          borderRadius: BorderRadius.circular(RtRadii.full),
        ),
        child: Text(
          _initials(name),
          style: RtTypography.avatarInitials.copyWith(
            color: colors.onSecondaryContainer,
          ),
        ),
      ),
    );
  }

  String _initials(String fullName) {
    final parts = fullName
        .trim()
        .split(RegExp(r'\s+'))
        .where((part) => part.isNotEmpty)
        .toList();
    if (parts.isEmpty) {
      return '';
    }
    final first = parts.first.substring(0, 1);
    if (parts.length == 1) {
      return first.toUpperCase();
    }
    final last = parts.last.substring(0, 1);
    return '$first$last'.toUpperCase();
  }
}
