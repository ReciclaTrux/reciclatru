import 'package:flutter/material.dart';

import '../atoms/rt_button.dart';
import '../theme/rt_spacing.dart';

/// Dirección, distrito y referencia del vecino, con Cómo llegar y Copiar
/// (HU-15).
///
/// Solo la ve el reciclador elegido mientras el pedido está Confirmado: la
/// pantalla no la muestra si la API no entregó el dato. Abrir la app de mapas
/// con la URI `geo:` ([lat], [lng]) y copiar al portapapeles son
/// responsabilidad de la pantalla, vía [onDirections] y [onCopy]; el bloque no
/// dibuja las coordenadas.
class RtAddressBlock extends StatelessWidget {
  const RtAddressBlock({
    super.key,
    required this.address,
    required this.district,
    required this.lat,
    required this.lng,
    required this.onDirections,
    required this.onCopy,
    this.reference,
  });

  /// Dirección escrita; nula si el vecino no la escribió.
  final String? address;
  final String district;

  /// Referencia opcional; sin ella esa parte no aparece.
  final String? reference;

  /// Coordenadas del punto de recojo, para la URI `geo:` de [onDirections].
  final double lat;
  final double lng;

  final VoidCallback onDirections;
  final VoidCallback onCopy;

  @override
  Widget build(BuildContext context) {
    final hasAddress = address != null && address!.trim().isNotEmpty;
    final hasReference = reference != null && reference!.trim().isNotEmpty;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _KeyValue(
          label: 'Dirección',
          value: hasAddress
              ? address!
              : 'Sin dirección escrita: usa «Cómo llegar» y la referencia',
          extra: district,
        ),
        if (hasReference) ...<Widget>[
          const SizedBox(height: RtSpacing.s3),
          _KeyValue(label: 'Referencia', value: reference!),
        ],
        const SizedBox(height: RtSpacing.s3),
        Row(
          children: <Widget>[
            Expanded(
              child: RtButton(
                text: 'Cómo llegar',
                icon: Icons.directions_rounded,
                onClick: onDirections,
                fullWidth: true,
              ),
            ),
            const SizedBox(width: RtSpacing.s2),
            RtButton(
              text: 'Copiar',
              icon: Icons.content_copy_rounded,
              variant: RtButtonVariant.secondary,
              onClick: onCopy,
            ),
          ],
        ),
      ],
    );
  }
}

class _KeyValue extends StatelessWidget {
  const _KeyValue({required this.label, required this.value, this.extra});

  final String label;
  final String value;
  final String? extra;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          label,
          style: theme.textTheme.labelLarge!.copyWith(
            color: colors.onSurfaceVariant,
          ),
        ),
        Text(
          value,
          style: theme.textTheme.bodyLarge!.copyWith(color: colors.onSurface),
        ),
        if (extra != null)
          Text(
            extra!,
            style: theme.textTheme.bodyMedium!.copyWith(
              color: colors.onSurfaceVariant,
            ),
          ),
      ],
    );
  }
}
