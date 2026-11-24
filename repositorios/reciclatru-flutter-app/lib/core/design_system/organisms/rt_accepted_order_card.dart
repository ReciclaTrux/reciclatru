import 'package:flutter/material.dart';

import '../atoms/rt_acceptance_chip.dart';
import '../molecules/rt_meta_row.dart';
import '../theme/rt_format.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Tarjeta de un pedido que el reciclador aceptó, con su etiqueta combinada
/// (HU-14).
///
/// La versión Elegido se destaca: borde de 2 px `secondary`, franja
/// `secondary-container` con "Te eligieron. Ve a recoger el material" y un pie
/// que lleva a HU-15. Las demás son tarjetas normales. No muestra dirección ni
/// teléfono: eso es del detalle.
class RtAcceptedOrderCard extends StatelessWidget {
  const RtAcceptedOrderCard({
    super.key,
    required this.orderId,
    required this.district,
    required this.totalWeightKg,
    required this.acceptedAt,
    required this.label,
    required this.onClick,
  });

  /// Identificador del pedido; da una clave estable para los tests de widget.
  final String orderId;

  final String district;
  final double totalWeightKg;

  /// Cuándo aceptó, ya formateado: "Aceptado hoy", "Aceptado ayer".
  final String acceptedAt;

  final RtAcceptanceLabel label;
  final VoidCallback onClick;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final chosen = label == RtAcceptanceLabel.chosen;
    final radius = BorderRadius.circular(RtRadii.lg);

    return KeyedSubtree(
      key: ValueKey<String>('accepted_order_card_$orderId'),
      child: DecoratedBox(
        decoration: BoxDecoration(
          color: colors.surface,
          borderRadius: radius,
          border: Border.all(
            color: chosen ? colors.secondary : colors.outlineVariant,
            width: chosen ? RtStroke.cardChosen : RtStroke.divider,
          ),
          boxShadow: chosen ? null : RtElevation.level1,
        ),
        child: ClipRRect(
          borderRadius: radius,
          child: Material(
            color: Colors.transparent,
            child: InkWell(
              onTap: onClick,
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  if (chosen) _buildBanner(theme),
                  Padding(
                    padding: const EdgeInsets.all(RtSpacing.s4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
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
                            const SizedBox(width: RtSpacing.s2),
                            Flexible(child: RtAcceptanceChip(label: label)),
                          ],
                        ),
                        const SizedBox(height: RtSpacing.s1),
                        RtMetaRow(
                          items: <RtMetaItem>[
                            RtMetaItem(
                              icon: Icons.scale_rounded,
                              text: RtFormat.kg(totalWeightKg),
                            ),
                            RtMetaItem(
                              icon: Icons.calendar_today_rounded,
                              text: acceptedAt,
                            ),
                          ],
                        ),
                        if (chosen) _buildFoot(theme),
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

  Widget _buildBanner(ThemeData theme) {
    final colors = theme.colorScheme;

    return ColoredBox(
      color: colors.secondaryContainer,
      child: Padding(
        padding: const EdgeInsets.symmetric(
          horizontal: RtSpacing.s4,
          vertical: RtSpacing.s2,
        ),
        child: Row(
          children: <Widget>[
            Icon(
              Icons.verified_rounded,
              size: RtSize.cardFootIcon,
              color: colors.onSecondaryContainer,
            ),
            const SizedBox(width: RtSpacing.s2),
            Expanded(
              child: Text(
                'Te eligieron. Ve a recoger el material',
                style: theme.textTheme.labelLarge!.copyWith(
                  fontWeight: FontWeight.w700,
                  color: colors.onSecondaryContainer,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFoot(ThemeData theme) {
    final colors = theme.colorScheme;

    return Container(
      margin: const EdgeInsets.only(top: RtSpacing.s3),
      padding: const EdgeInsets.only(top: RtSpacing.s3),
      decoration: BoxDecoration(
        border: Border(top: BorderSide(color: colors.outlineVariant)),
      ),
      child: Row(
        children: <Widget>[
          Icon(
            Icons.directions_rounded,
            size: RtSize.cardFootIcon,
            color: colors.onSurfaceVariant,
          ),
          const SizedBox(width: RtSpacing.s2),
          Expanded(
            child: Text(
              'Ver dirección y cómo llegar',
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: theme.textTheme.labelLarge!.copyWith(
                color: colors.onSurface,
              ),
            ),
          ),
          Icon(
            Icons.chevron_right_rounded,
            size: RtSize.cardFootIcon,
            color: colors.onSurfaceVariant,
          ),
        ],
      ),
    );
  }
}
