/// Radios del sistema (`tokens.json`, `radius`).
abstract final class RtRadii {
  static const double sm = 8;
  static const double md = 12;
  static const double lg = 16;
  static const double full = 999;
}

/// Grosores de borde y de trazo del sistema (`bundle.css`).
abstract final class RtStroke {
  /// Anillo de foco y borde de control enfocado.
  static const double thin = 1;

  /// Borde de campos y controles (`outline-strong`).
  static const double control = 1.5;

  /// Anillo de foco de 2 px.
  static const double focus = 2;

  /// Trazo de [RtSpinner] pequeño.
  static const double spinnerSmall = 2.5;

  /// Trazo de [RtSpinner] grande.
  static const double spinnerLarge = 3;

  /// Divisor de 1 px (`--outline`).
  static const double divider = 1;

  /// Borde de la opción elegida en `RtRoleSelector`.
  static const double roleSelected = 2;

  /// Anillo del radio de `RtRoleSelector` sin elegir y su relleno al elegir.
  static const double roleRadio = 2;
  static const double roleRadioSelected = 7;
}
