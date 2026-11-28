/// Define el componente reutilizable rt acceptance card del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import '../atoms/rt_avatar.dart';

/// Tarjeta de un reciclador que aceptó recoger el pedido.
class RtAcceptanceCard extends StatelessWidget {
  const RtAcceptanceCard({
    required this.name,
    required this.phone,
    required this.acceptedAt,
    this.onCall,
    this.onChoose,
    this.choosing = false,
    this.chooseEnabled = true,
    super.key,
  });
  final String name;
  final String phone;
  final String acceptedAt;
  final VoidCallback? onCall;
  final VoidCallback? onChoose;
  final bool choosing;
  final bool chooseEnabled;

  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(12),
      child: Row(children: [
        RtAvatar(name: name),
        const SizedBox(width: 8),
        Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text(name, style: Theme.of(context).textTheme.titleSmall),
          Text('${_groupPhone(phone)} · $acceptedAt', style: Theme.of(context).textTheme.bodySmall),
        ])),
        IconButton(tooltip: 'Llamar a $name', onPressed: onCall, icon: const Icon(Icons.call_rounded)),
        if (onChoose != null)
          FilledButton(
            onPressed: choosing || !chooseEnabled ? null : onChoose,
            child: choosing ? const SizedBox.square(dimension: 18, child: CircularProgressIndicator(strokeWidth: 2)) : const Text('Elegir'),
          ),
      ]),
    ),
  );

  /// Agrupa un número peruano de nueve cifras en bloques de tres.
  String _groupPhone(String value) {
    final digits = value.replaceAll(RegExp(r'\D'), '');
    if (digits.length != 9) return value;
    return '${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}';
  }
}
