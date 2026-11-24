import 'package:flutter/material.dart';

import '../atoms/rt_icon_button.dart';
import '../atoms/rt_text_field.dart';
import '../theme/rt_spacing.dart';

/// Una fila editable de ítem del pedido (HU-05, HU-07).
///
/// Descripción, peso en kg y botón para quitarla. Los errores se muestran
/// debajo de la fila, en una sola línea. Si es el único ítem, quien la usa pasa
/// [canRemove] en `false` y el botón de quitar no aparece.
class RtItemRow extends StatelessWidget {
  const RtItemRow({
    super.key,
    required this.description,
    required this.weightKg,
    required this.onChange,
    this.onRemove,
    this.descriptionError,
    this.weightError,
    this.canRemove = true,
  });

  /// Material escrito por la persona; hasta 100 caracteres.
  final String description;

  /// Peso escrito por la persona; mayor a 0 y hasta 500, un decimal.
  final String weightKg;

  /// Devuelve la fila completa cada vez que cambia un campo.
  final void Function(String description, String weightKg) onChange;

  final VoidCallback? onRemove;

  final String? descriptionError;
  final String? weightError;

  /// `false` cuando es el único ítem del pedido.
  final bool canRemove;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final showRemove = canRemove && onRemove != null;
    final errors = <String>[
      if (descriptionError != null && descriptionError!.isNotEmpty)
        descriptionError!,
      if (weightError != null && weightError!.isNotEmpty) weightError!,
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: <Widget>[
            Expanded(
              child: RtTextField(
                label: 'Material',
                value: description,
                onValueChange: (value) => onChange(value, weightKg),
                placeholder: 'Botellas de plástico',
              ),
            ),
            const SizedBox(width: RtSpacing.s2),
            SizedBox(
              width: RtSize.itemWeightColumn,
              child: RtTextField(
                label: 'Peso',
                value: weightKg,
                onValueChange: (value) => onChange(description, value),
                suffix: 'kg',
                keyboardType: const TextInputType.numberWithOptions(
                  decimal: true,
                ),
              ),
            ),
            if (showRemove) ...<Widget>[
              const SizedBox(width: RtSpacing.s2),
              Padding(
                padding: const EdgeInsets.only(top: RtSize.itemRemoveOffset),
                child: RtIconButton(
                  icon: Icons.delete_rounded,
                  contentDescription: 'Quitar ítem',
                  onClick: onRemove!,
                ),
              ),
            ],
          ],
        ),
        if (errors.isNotEmpty) ...<Widget>[
          const SizedBox(height: RtSpacing.fieldLabelGap),
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
                  errors.join(' '),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: theme.textTheme.labelMedium!.copyWith(
                    color: colors.error,
                  ),
                ),
              ),
            ],
          ),
        ],
      ],
    );
  }
}
