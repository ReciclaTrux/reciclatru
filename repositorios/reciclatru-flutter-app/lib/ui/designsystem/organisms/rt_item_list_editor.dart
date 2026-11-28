/// Define el componente reutilizable rt item list editor del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';
import '../molecules/rt_item_row.dart';
import '../atoms/rt_button.dart';
import '../theme/rt_spacing.dart';

/// Datos visibles de una fila editable.
class RtItemListEntry {
  const RtItemListEntry({
    required this.description,
    required this.weight,
    this.error,
    this.descriptionInvalid = false,
    this.weightInvalid = false,
  });
  final String description;
  final String weight;
  final String? error;
  final bool descriptionInvalid;
  final bool weightInvalid;
}

/// Editor de materiales con contador y recuadro de peso total igual a Kotlin.
class RtItemListEditor extends StatelessWidget {
  const RtItemListEditor({
    required this.items,
    required this.totalWeight,
    required this.onDescriptionChange,
    required this.onWeightChange,
    required this.onRemove,
    required this.onAdd,
    this.maxItems = 10,
    this.error,
    super.key,
  });

  final List<RtItemListEntry> items;
  final String totalWeight;
  final void Function(int, String) onDescriptionChange;
  final void Function(int, String) onWeightChange;
  final ValueChanged<int> onRemove;
  final VoidCallback onAdd;
  final int maxItems;
  final String? error;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final canAdd = items.length < maxItems;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Text(
          'Materiales',
          style: Theme.of(context).textTheme.labelLarge?.copyWith(
            color: colors.onSurfaceVariant,
          ),
        ),
        for (var index = 0; index < items.length; index++) ...[
          const SizedBox(height: RtSpacing.s3),
          RtItemRow(
            description: items[index].description,
            onDescriptionChange: (value) => onDescriptionChange(index, value),
            weight: items[index].weight,
            onWeightChange: (value) => onWeightChange(index, value),
            onRemove: () => onRemove(index),
            error: items[index].error,
            descriptionInvalid: items[index].descriptionInvalid,
            weightInvalid: items[index].weightInvalid,
          ),
        ],
        const SizedBox(height: RtSpacing.s2),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            RtButton(
              text: 'Agregar ítem',
              onClick: onAdd,
              variant: RtButtonVariant.text,
              icon: Icons.add_rounded,
              enabled: canAdd,
            ),
            Text(
              '${items.length} de $maxItems ítems',
              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
          ],
        ),
        if (error != null) ...[
          const SizedBox(height: RtSpacing.s1),
          Row(
            children: [
              Icon(Icons.error_rounded, size: RtSize.iconXs, color: colors.error),
              const SizedBox(width: RtSpacing.s1),
              Expanded(
                child: Text(
                  error!,
                  style: Theme.of(context).textTheme.labelMedium?.copyWith(
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
            children: [
              Text(
                'Peso total',
                style: Theme.of(context).textTheme.labelLarge?.copyWith(
                  color: colors.onPrimaryContainer,
                ),
              ),
              Text(
                totalWeight,
                style: Theme.of(context).textTheme.titleLarge?.copyWith(
                  color: colors.onPrimaryContainer,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}
