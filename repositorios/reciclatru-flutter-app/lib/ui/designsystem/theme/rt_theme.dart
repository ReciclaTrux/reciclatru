/// Define el token visual rt theme reutilizado por las pantallas del sistema de diseño.
import 'package:flutter/material.dart';

import 'rt_colors.dart';
import 'rt_typography.dart';

/// Tema del sistema de diseño de ReciclaTru.
///
/// Los valores salen de `tokens.json`; el mapeo a Material 3 y la extensión
/// propia están en `implementacion.md`. Se aplica en `MaterialApp.theme`.
final ThemeData rtTheme = ThemeData(
  useMaterial3: true,
  colorScheme: rtColorScheme,
  scaffoldBackgroundColor: rtBackground,
  textTheme: RtTypography.textTheme,
  extensions: const <ThemeExtension<dynamic>>[RtExtraColors()],
);

