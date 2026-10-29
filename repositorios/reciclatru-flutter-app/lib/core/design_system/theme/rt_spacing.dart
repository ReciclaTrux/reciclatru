/// Espaciado del sistema: grilla de 4 (`tokens.json`, `spacing`).
abstract final class RtSpacing {
  static const double s1 = 4;
  static const double s2 = 8;
  static const double s3 = 12;
  static const double s4 = 16;
  static const double s5 = 24;
  static const double s6 = 32;
  static const double s7 = 48;

  /// Separación entre la etiqueta y el campo, y entre el campo y su ayuda
  /// (`bundle.css`, `.rt-field`).
  static const double fieldLabelGap = 6;
}

/// Tamaños fijos del sistema (`tokens.json`, `size`).
abstract final class RtSize {
  static const double touchTarget = 48;
  static const double buttonHeight = 52;
  static const double buttonHeightSm = 40;
  static const double inputHeight = 56;
  static const double appbarHeight = 64;
  static const double bottomnavHeight = 80;
  static const double avatarMd = 40;
  static const double thumbSm = 56;
  static const double thumbMd = 80;
  static const double thumbLg = 112;
  static const double iconSm = 18;
  static const double iconMd = 24;

  /// Íconos de etiquetas y ayudas (16 px en la vista previa del sistema).
  static const double iconXs = 16;

  /// Alto de [RtBadge] (`bundle.css`, `.rt-badge`).
  static const double badgeHeight = 24;

  /// Área de texto larga de [RtTextField] (`bundle.css`, `.rt-input--area`).
  static const double inputAreaMinHeight = 96;

  /// Diámetros de [RtSpinner] (`components/RtSpinner/README.md`).
  static const double spinnerSmall = 20;
  static const double spinnerLarge = 32;

  /// Ancho máximo de `RtConfirmDialog` (`.rt-dialog`).
  static const double dialogMaxWidth = 328;

  /// Círculo e ícono de `RtErrorState` (`.rt-state__icon`).
  static const double stateIcon = 72;
  static const double stateIconGlyph = 36;

  /// Ancho máximo del mensaje de `RtErrorState` (`.rt-state__body`).
  static const double stateMessageMaxWidth = 280;

  /// Alto de `RtMapPicker` (`.rt-map`).
  static const double mapHeight = 420;

  /// Pin de `RtMapPicker` (`.rt-map__pin .rt-icon`).
  static const double mapPin = 48;

  /// Píldora del destino de `RtBottomNav` (`.rt-navitem__pill`).
  static const double navPillWidth = 64;
  static const double navPillHeight = 32;

  /// Radio de la opción de `RtRoleSelector` (`.rt-role__radio`).
  static const double roleRadio = 22;
}
