import 'package:flutter/material.dart';

import '../atoms/rt_badge.dart';
import '../atoms/rt_photo_thumb.dart';
import '../molecules/rt_meta_row.dart';
import '../theme/rt_format.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Tarjeta de un pedido disponible para el reciclador (HU-10).
///
/// El título es el distrito: nunca la dirección ni el nombre del vecino
/// (privacidad). Si el reciclador ya aceptó, muestra [RtBadge] "Ya aceptaste".
/// Sin ubicación del reciclador no se pasa [distanceKm] y se omite la
/// distancia.
class RtAvailableOrderCard extends StatelessWidget {
  const RtAvailableOrderCard({
    super.key,
    required this.id,
    required this.district,
    required this.totalWeightKg,
    required this.itemsCount,
    required this.publishedAgo,
    required this.onClick,
    this.distanceKm,
    this.photoUrl,
    this.alreadyAccepted = false,
  });

  /// Identificador del pedido; da una clave estable para los tests de widget.
  final String id;

  final String district;

  /// Distancia al reciclador; nula si no dio su ubicación.
  final double? distanceKm;

  final double totalWeightKg;
  final int itemsCount;
  final String? photoUrl;

  /// Antigüedad ya formateada: "hace 2 h", "hace 20 min".
  final String publishedAgo;

  /// Si el reciclador ya aceptó este pedido.
  final bool alreadyAccepted;

  final VoidCallback onClick;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return KeyedSubtree(
      key: ValueKey<String>('available_order_card_$id'),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: colors.surface,
          borderRadius: BorderRadius.circular(RtRadii.lg),
          border: Border.all(color: colors.outlineVariant),
          boxShadow: RtElevation.level1,
        ),
        child: Material(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(RtRadii.lg),
          child: InkWell(
            onTap: onClick,
            borderRadius: BorderRadius.circular(RtRadii.lg),
            child: Padding(
              padding: const EdgeInsets.all(RtSpacing.s4),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: <Widget>[
                  RtPhotoThumb(url: photoUrl),
                  const SizedBox(width: RtSpacing.s3),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: <Widget>[
                        Row(
                          children: <Widget>[
                            Expanded(
                              child: Text(
                                district,
                                maxLines: 1,
                                overflow: TextOverflow.ellipsis,
                                style: theme.textTheme.titleMedium!.copyWith(
                                  color: colors.onSurface,
                                ),
                              ),
                            ),
                            if (alreadyAccepted) ...<Widget>[
                              const SizedBox(width: RtSpacing.s2),
                              const RtBadge(
                                text: 'Ya aceptaste',
                                icon: Icons.check_rounded,
                              ),
                            ],
                          ],
                        ),
                        const SizedBox(height: RtSpacing.s1),
                        RtMetaRow(
                          items: <RtMetaItem>[
                            if (distanceKm != null)
                              RtMetaItem(
                                icon: Icons.near_me_rounded,
                                text: RtFormat.distanceKm(distanceKm!),
                              ),
                            RtMetaItem(
                              icon: Icons.scale_rounded,
                              text: RtFormat.kg(totalWeightKg),
                            ),
                            RtMetaItem(
                              icon: Icons.inventory_2_rounded,
                              text: RtFormat.items(itemsCount),
                            ),
                            RtMetaItem(
                              icon: Icons.schedule_rounded,
                              text: publishedAgo,
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
