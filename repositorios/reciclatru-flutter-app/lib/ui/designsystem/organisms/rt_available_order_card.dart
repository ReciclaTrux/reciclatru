/// Define el componente reutilizable rt available order card del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_badge.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_photo_thumb.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_meta_row.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Tarjeta de pedido disponible; no presenta contacto ni dirección exacta.
class RtAvailableOrderCard extends StatelessWidget {
  const RtAvailableOrderCard({
    super.key,
    required this.zone,
    required this.weight,
    required this.items,
    this.distance,
    this.timeAgo,
    this.alreadyAccepted = false,
    this.onClick,
    this.thumbnail,
  });

  final String zone;
  final String weight;
  final String items;
  final String? distance;
  final String? timeAgo;
  final bool alreadyAccepted;
  final VoidCallback? onClick;
  final String? thumbnail;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    return Material(
      color: colors.surface,
      borderRadius: BorderRadius.circular(RtRadii.lg),
      child: InkWell(
        onTap: onClick,
        borderRadius: BorderRadius.circular(RtRadii.lg),
        child: Container(
          padding: const EdgeInsets.all(RtSpacing.s3),
          decoration: BoxDecoration(
            border: Border.all(color: colors.outlineVariant),
            borderRadius: BorderRadius.circular(RtRadii.lg),
          ),
          child: Row(
            children: [
              RtPhotoThumb(model: thumbnail),
              const SizedBox(width: RtSpacing.s3),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(child: Text(zone, maxLines: 1, overflow: TextOverflow.ellipsis, style: theme.textTheme.titleMedium)),
                        if (alreadyAccepted) const RtBadge(text: 'Ya aceptaste', icon: Icons.check_rounded),
                      ],
                    ),
                    const SizedBox(height: RtSpacing.s1),
                    RtMetaRow(items: [
                      if (distance != null) RtMeta(Icons.near_me_rounded, distance!),
                      RtMeta(Icons.scale_rounded, weight),
                      RtMeta(Icons.inventory_2_rounded, items),
                    ]),
                    if (timeAgo != null) ...[
                      const SizedBox(height: RtSpacing.s1),
                      RtMetaRow(items: [RtMeta(Icons.schedule_rounded, timeAgo!)]),
                    ],
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
