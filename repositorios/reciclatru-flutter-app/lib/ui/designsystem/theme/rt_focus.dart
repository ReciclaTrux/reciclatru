/// Define el token visual rt focus reutilizado por las pantallas del sistema de diseño.
import 'package:flutter/material.dart';

/// Indicador visible de foco teclado con separación alrededor del control.
class RtFocusRing extends StatelessWidget {
  const RtFocusRing({
    required this.visible,
    required this.child,
    this.color,
    this.borderRadius = const BorderRadius.all(Radius.circular(12)),
    super.key,
  });

  final bool visible;
  final Widget child;
  final Color? color;
  final BorderRadius borderRadius;

  @override
  Widget build(BuildContext context) {
    final ringColor = color ?? Theme.of(context).colorScheme.primary;
    return DecoratedBox(
      decoration: BoxDecoration(
        borderRadius: borderRadius,
        boxShadow: visible
            ? [BoxShadow(color: ringColor, spreadRadius: 2, blurRadius: 0)]
            : const [],
      ),
      child: child,
    );
  }
}
