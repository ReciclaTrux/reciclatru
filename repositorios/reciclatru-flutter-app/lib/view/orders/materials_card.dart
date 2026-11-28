/// Construye la pantalla o navegación de materials card conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';

import 'package:reciclatru/model/orders/order.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_meta_row.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Presenta los materiales y su peso total en el detalle de un pedido.
class MaterialsCard extends StatelessWidget {
  const MaterialsCard({super.key, required this.items, required this.totalWeightKg});
  final List<OrderItem> items;
  final double totalWeightKg;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Container(
      padding: const EdgeInsets.all(RtSpacing.s4),
      decoration: BoxDecoration(
        color: colors.surface,
        border: Border.all(color: colors.outlineVariant),
        borderRadius: BorderRadius.circular(RtRadii.lg),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Text('Materiales', style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: RtSpacing.s3),
          for (final item in items) ...[
            Row(children: [
              Expanded(child: Text(item.description)),
              Text('${item.weightKg} kg'),
            ]),
            const SizedBox(height: RtSpacing.s2),
          ],
          const Divider(),
          RtMetaRow(items: [RtMeta(Icons.scale_rounded, 'Total: $totalWeightKg kg')]),
        ],
      ),
    );
  }
}
