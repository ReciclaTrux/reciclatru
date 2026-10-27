import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

import 'rt_colors.dart';

/// Tipografía del sistema: Nunito Sans con los estilos de `tokens.json`.
///
/// El mapeo a Material 3 está en `implementacion.md`; `body-strong`, `button` y
/// las iniciales del avatar no tienen rol en `TextTheme` y se exponen aparte.
abstract final class RtTypography {
  /// Estilos mapeados a Material 3 (`implementacion.md`, "Tipografía en Material").
  static final TextTheme textTheme = GoogleFonts.nunitoSansTextTheme().copyWith(
    headlineMedium: _style(28, 36, FontWeight.w700), // headline-lg
    headlineSmall: _style(24, 32, FontWeight.w700), // headline
    titleLarge: _style(20, 28, FontWeight.w700), // title
    titleMedium: _style(18, 24, FontWeight.w600), // subtitle
    bodyLarge: _style(16, 24, FontWeight.w400), // body
    bodyMedium: _style(14, 20, FontWeight.w400), // body-sm
    labelLarge: _style(14, 20, FontWeight.w600), // label
    labelMedium: _style(13, 18, FontWeight.w600), // caption
  );

  /// `body-strong`: nombres y datos que se ubican rápido.
  static TextStyle get bodyStrong => _style(16, 24, FontWeight.w600);

  /// `button`: texto de botones.
  static TextStyle get button => _style(16, 24, FontWeight.w700);

  /// Iniciales de [RtAvatar]: 15/15 700 según la vista previa del sistema.
  static TextStyle get avatarInitials => _style(15, 15, FontWeight.w700);

  static TextStyle _style(
    double fontSize,
    double lineHeight,
    FontWeight fontWeight,
  ) {
    return GoogleFonts.nunitoSans(
      fontSize: fontSize,
      height: lineHeight / fontSize,
      fontWeight: fontWeight,
      color: rtColorScheme.onSurface,
    );
  }
}
