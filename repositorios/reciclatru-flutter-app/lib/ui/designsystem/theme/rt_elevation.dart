/// Define el token visual rt elevation reutilizado por las pantallas del sistema de diseño.
import 'package:flutter/material.dart';

/// Niveles de elevación equivalentes a los tokens Compose.
abstract final class RtElevation {
  static const List<BoxShadow> level1 = <BoxShadow>[
    BoxShadow(color: Color(0x1A1A1C1A), offset: Offset(0, 1), blurRadius: 2),
    BoxShadow(color: Color(0x0F1A1C1A), offset: Offset(0, 1), blurRadius: 3),
  ];
  static const List<BoxShadow> level2 = <BoxShadow>[
    BoxShadow(color: Color(0x1F1A1C1A), offset: Offset(0, 2), blurRadius: 6),
    BoxShadow(color: Color(0x141A1C1A), offset: Offset(0, 1), blurRadius: 2),
  ];
  static const List<BoxShadow> level3 = <BoxShadow>[
    BoxShadow(color: Color(0x2E1A1C1A), offset: Offset(0, 8), blurRadius: 24),
    BoxShadow(color: Color(0x1A1A1C1A), offset: Offset(0, 2), blurRadius: 6),
  ];
}
