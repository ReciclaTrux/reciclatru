/// Define el token visual rt shapes reutilizado por las pantallas del sistema de diseño.
import 'package:flutter/material.dart';

import 'rt_spacing.dart';

/// Formas Material 3 derivadas de los radios compartidos.
abstract final class RtShapes {
  static final ShapeBorder extraSmall = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(RtRadii.sm),
  );
  static final ShapeBorder small = extraSmall;
  static final ShapeBorder medium = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(RtRadii.md),
  );
  static final ShapeBorder large = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(RtRadii.lg),
  );
  static final ShapeBorder extraLarge = RoundedRectangleBorder(
    borderRadius: BorderRadius.circular(RtRadii.full),
  );
}
