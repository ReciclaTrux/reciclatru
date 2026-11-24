import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../molecules/rt_contact_row.dart';
import '../theme/rt_format.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Fila de un reciclador que aceptó el pedido del vecino (HU-13).
///
/// Lleva el botón de llamada y Elegir. Mientras [choosing] está activo el
/// botón muestra la carga; [chooseEnabled] permite bloquear los Elegir de las
/// demás filas hasta tener respuesta (solo vale la primera elección). Elegir
/// abre `RtConfirmDialog` desde la pantalla.
class RtAcceptanceCard extends StatelessWidget {
  const RtAcceptanceCard({
    super.key,
    required this.recyclerName,
    required this.phone,
    required this.acceptedAt,
    required this.onCall,
    required this.onChoose,
    this.choosing = false,
    this.chooseEnabled = true,
  });

  final String recyclerName;

  /// Celular del reciclador; el vecino lo ve porque el reciclador aceptó.
  final String phone;

  /// Cuándo aceptó, ya formateado: "hoy, 9:40".
  final String acceptedAt;

  final VoidCallback onCall;
  final VoidCallback onChoose;

  /// Muestra la carga en el botón Elegir.
  final bool choosing;

  /// En falso cuando otra fila está eligiendo.
  final bool chooseEnabled;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return DecoratedBox(
      decoration: BoxDecoration(
        color: colors.surface,
        borderRadius: BorderRadius.circular(RtRadii.lg),
        border: Border.all(color: colors.outlineVariant),
        boxShadow: RtElevation.level1,
      ),
      child: Padding(
        padding: const EdgeInsets.all(RtSpacing.s4),
        child: Row(
          children: <Widget>[
            Expanded(
              child: RtContactRow(
                name: recyclerName,
                subtitle: '${RtFormat.phone(phone)} · $acceptedAt',
                phone: phone,
                onCall: onCall,
              ),
            ),
            const SizedBox(width: RtSpacing.s2),
            RtButton(
              text: 'Elegir',
              size: RtButtonSize.compact,
              loading: choosing,
              enabled: chooseEnabled,
              onClick: onChoose,
            ),
          ],
        ),
      ),
    );
  }
}
