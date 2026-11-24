import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../molecules/rt_item_row.dart';
import '../theme/rt_format.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';

/// Un ítem del pedido dentro de [RtItemListEditor].
class RtOrderItem {
  const RtOrderItem({
    this.description = '',
    this.weightKg = '',
    this.descriptionError,
    this.weightError,
  });

  final String description;
  final String weightKg;
  final String? descriptionError;
  final String? weightError;
}

/// Lista editable de ítems del pedido con Agregar ítem y el peso total en vivo
/// (HU-05, HU-07).
///
/// Empieza con una fila vacía. Al llegar a [maxItems], Agregar ítem desaparece
/// y queda "10 de 10 ítems". El peso total se recalcula con cada cambio y solo
/// suma pesos válidos (mayor a 0 y hasta 500 kg).
class RtItemListEditor extends StatelessWidget {
  const RtItemListEditor({
    super.key,
    required this.items,
    required this.onAdd,
    required this.onChange,
    required this.onRemove,
    this.listError,
  });

  /// Máximo de ítems de un pedido en el Release 1.
  static const int maxItems = 10;

  /// Peso máximo válido de un ítem, en kg.
  static const double maxWeightKg = 500;

  final List<RtOrderItem> items;
  final VoidCallback onAdd;
  final void Function(int index, String description, String weightKg) onChange;
  final ValueChanged<int> onRemove;

  /// "Agrega al menos un ítem".
  final String? listError;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final canAdd = items.length < maxItems;
    final hasListError = listError != null && listError!.isNotEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Text(
          'Materiales',
          style: theme.textTheme.labelLarge!.copyWith(
            color: colors.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: RtSpacing.s2),
        for (var index = 0; index < items.length; index++) ...<Widget>[
          if (index > 0) const SizedBox(height: RtSpacing.s3),
          RtItemRow(
            description: items[index].description,
            weightKg: items[index].weightKg,
            descriptionError: items[index].descriptionError,
            weightError: items[index].weightError,
            canRemove: items.length > 1,
            onRemove: () => onRemove(index),
            onChange: (description, weightKg) =>
                onChange(index, description, weightKg),
          ),
        ],
        const SizedBox(height: RtSpacing.s3),
        Row(
          children: <Widget>[
            if (canAdd)
              Expanded(
                child: Align(
                  alignment: Alignment.centerLeft,
                  child: RtButton(
                    text: 'Agregar ítem',
                    variant: RtButtonVariant.text,
                    icon: Icons.add_rounded,
                    onClick: onAdd,
                  ),
                ),
              )
            else
              const Spacer(),
            Text(
              '${items.length} de $maxItems ítems',
              style: theme.textTheme.bodyMedium!.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
          ],
        ),
        if (hasListError) ...<Widget>[
          const SizedBox(height: RtSpacing.s2),
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
                  listError!,
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
        const SizedBox(height: RtSpacing.s3),
        Container(
          padding: const EdgeInsets.symmetric(
            horizontal: RtSpacing.s4,
            vertical: RtSpacing.s3,
          ),
          decoration: BoxDecoration(
            color: colors.primaryContainer,
            borderRadius: BorderRadius.circular(RtRadii.md),
          ),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: <Widget>[
              Text(
                'Peso total',
                style: theme.textTheme.labelLarge!.copyWith(
                  color: colors.onPrimaryContainer,
                ),
              ),
              Text(
                RtFormat.kg(_totalWeightKg()),
                style: theme.textTheme.headlineSmall!.copyWith(
                  color: colors.onPrimaryContainer,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  double _totalWeightKg() {
    var total = 0.0;
    for (final item in items) {
      final value = double.tryParse(item.weightKg.replaceAll(',', '.'));
      if (value != null && value > 0 && value <= maxWeightKg) {
        total += value;
      }
    }
    return total;
  }
}
