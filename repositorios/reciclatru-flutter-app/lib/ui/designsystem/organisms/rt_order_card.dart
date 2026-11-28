/// Define el componente reutilizable rt order card del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import '../atoms/rt_status_chip.dart';
import '../atoms/rt_photo_thumb.dart';

/// Pie opcional con información del reciclador o las aceptaciones.
class RtOrderCardFooter {
  const RtOrderCardFooter({required this.icon, required this.text, this.actionLabel});
  final IconData icon;
  final String text;
  final String? actionLabel;
}

/// Tarjeta de pedido del vecino; toda la superficie abre el detalle.
class RtOrderCard extends StatelessWidget {
  const RtOrderCard({
    required this.title,
    required this.status,
    required this.items,
    required this.weight,
    required this.onClick,
    this.footer,
    this.thumbnail,
    super.key,
  });
  final String title;
  final RtOrderStatus status;
  final String items;
  final String weight;
  final VoidCallback onClick;
  final RtOrderCardFooter? footer;
  final String? thumbnail;

  @override
  Widget build(BuildContext context) => Card(
    child: InkWell(
      onTap: onClick,
      borderRadius: BorderRadius.circular(16),
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Row(children: [
            RtPhotoThumb(model: thumbnail, size: 56),
            const SizedBox(width: 12),
            Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
              Text(title, style: Theme.of(context).textTheme.titleSmall),
              Text('$items · $weight'),
            ])),
            RtStatusChip(status: status),
          ]),
          if (footer != null) ...[
            const Divider(height: 20),
            Row(children: [Icon(footer!.icon), const SizedBox(width: 8), Expanded(child: Text(footer!.text)), if (footer!.actionLabel != null) Text(footer!.actionLabel!)]),
          ],
        ]),
      ),
    ),
  );
}
