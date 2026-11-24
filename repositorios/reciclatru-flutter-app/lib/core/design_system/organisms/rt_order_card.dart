import 'package:flutter/material.dart';

import '../atoms/rt_photo_thumb.dart';
import '../atoms/rt_status_chip.dart';
import '../molecules/rt_meta_row.dart';
import '../theme/rt_format.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Tarjeta de un pedido en la lista del vecino (HU-06).
///
/// Muestra la primera foto (o el estado Sin foto de [RtPhotoThumb]), la fecha,
/// el estado, los ítems y el peso. El pie cambia con el estado: Pendiente
/// muestra las aceptaciones; Confirmado, el reciclador elegido; Recolectado y
/// Cancelado no tienen pie. No muestra la dirección: el vecino ya sabe dónde
/// vive. Toda la tarjeta es tocable y lleva al detalle.
class RtOrderCard extends StatelessWidget {
  const RtOrderCard({
    super.key,
    required this.id,
    required this.date,
    required this.status,
    required this.itemsCount,
    required this.totalWeightKg,
    required this.onClick,
    this.photoUrl,
    this.acceptances = 0,
    this.recyclerName,
  });

  /// Identificador del pedido; da una clave estable para los tests de widget.
  final String id;

  /// Fecha ya formateada por el sistema: "Hoy, 9:15", "ayer", "10 nov".
  final String date;

  final RtOrderStatus status;
  final int itemsCount;
  final double totalWeightKg;

  /// Primera foto del pedido; sin ella se muestra el estado Sin foto.
  final String? photoUrl;

  /// Aceptaciones recibidas; solo se muestran si el pedido está Pendiente.
  final int acceptances;

  /// Nombre del reciclador elegido; solo se muestra si está Confirmado.
  final String? recyclerName;

  final VoidCallback onClick;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return KeyedSubtree(
      key: ValueKey<String>('order_card_$id'),
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
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Row(
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
                                    date,
                                    maxLines: 1,
                                    overflow: TextOverflow.ellipsis,
                                    style: theme.textTheme.titleMedium!
                                        .copyWith(color: colors.onSurface),
                                  ),
                                ),
                                const SizedBox(width: RtSpacing.s2),
                                RtStatusChip(status: status),
                              ],
                            ),
                            const SizedBox(height: RtSpacing.s1),
                            RtMetaRow(
                              items: <RtMetaItem>[
                                RtMetaItem(
                                  icon: Icons.inventory_2_rounded,
                                  text: itemsCount == 1
                                      ? '1 ítem'
                                      : '$itemsCount ítems',
                                ),
                                RtMetaItem(
                                  icon: Icons.scale_rounded,
                                  text: RtFormat.kg(totalWeightKg),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  _buildFoot(theme),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildFoot(ThemeData theme) {
    final colors = theme.colorScheme;
    final foot = switch (status) {
      RtOrderStatus.pending => _pendingFoot(theme),
      RtOrderStatus.confirmed => _confirmedFoot(theme),
      RtOrderStatus.collected || RtOrderStatus.cancelled => null,
    };
    if (foot == null) {
      return const SizedBox.shrink();
    }

    return Container(
      margin: const EdgeInsets.only(top: RtSpacing.s3),
      padding: const EdgeInsets.only(top: RtSpacing.s3),
      decoration: BoxDecoration(
        border: Border(top: BorderSide(color: colors.outlineVariant)),
      ),
      child: foot,
    );
  }

  Widget _pendingFoot(ThemeData theme) {
    final colors = theme.colorScheme;
    final hasAcceptances = acceptances > 0;

    return Row(
      children: <Widget>[
        Icon(
          Icons.group_rounded,
          size: RtSize.cardFootIcon,
          color: colors.onSurfaceVariant,
        ),
        const SizedBox(width: RtSpacing.s2),
        Expanded(
          child: Text(
            hasAcceptances
                ? '$acceptances ${acceptances == 1 ? 'aceptación' : 'aceptaciones'}'
                : 'Sin aceptaciones aún',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: hasAcceptances
                ? theme.textTheme.labelLarge!.copyWith(color: colors.onSurface)
                : theme.textTheme.bodyMedium!.copyWith(
                    color: colors.onSurfaceVariant,
                  ),
          ),
        ),
        if (hasAcceptances) ...<Widget>[
          const SizedBox(width: RtSpacing.s2),
          Text(
            'Elegir reciclador',
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: theme.textTheme.labelLarge!.copyWith(
              color: colors.onSurfaceVariant,
            ),
          ),
        ],
        const SizedBox(width: RtSpacing.s1),
        Icon(
          Icons.chevron_right_rounded,
          size: RtSize.cardFootIcon,
          color: colors.onSurfaceVariant,
        ),
      ],
    );
  }

  Widget _confirmedFoot(ThemeData theme) {
    final colors = theme.colorScheme;
    final name = recyclerName ?? '';

    return Row(
      children: <Widget>[
        Icon(
          Icons.person_rounded,
          size: RtSize.cardFootIcon,
          color: colors.onSurfaceVariant,
        ),
        const SizedBox(width: RtSpacing.s2),
        Expanded(
          child: Text(
            name,
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
            style: theme.textTheme.labelLarge!.copyWith(
              color: colors.onSurface,
            ),
          ),
        ),
        const SizedBox(width: RtSpacing.s1),
        Icon(
          Icons.chevron_right_rounded,
          size: RtSize.cardFootIcon,
          color: colors.onSurfaceVariant,
        ),
      ],
    );
  }
}
