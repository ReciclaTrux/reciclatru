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

/// Sombras del sistema (`tokens.json`, `shadow`).
///
/// Solo las usan tarjetas de lista (`elevation-1`), la barra de acción fija y
/// el snackbar (`elevation-2`), y los diálogos y la hoja del mapa
/// (`elevation-3`). Ningún otro elemento tiene sombra.
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
