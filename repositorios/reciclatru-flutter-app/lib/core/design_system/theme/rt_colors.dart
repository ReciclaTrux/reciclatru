import 'package:flutter/material.dart';

/// Fondo de pantalla del sistema (`background` en `tokens.json`).
///
/// Material 3 ya no expone un rol `background`; se aplica como
/// `scaffoldBackgroundColor` en [rtTheme].
const Color rtBackground = Color(0xFFF7F8F5);

/// Tokens de color mapeados a Material 3, según `implementacion.md`.
const ColorScheme rtColorScheme = ColorScheme(
  brightness: Brightness.light,
  primary: Color(0xFF8C6418),
  onPrimary: Color(0xFFFFFFFF),
  primaryContainer: Color(0xFFF6E7C8),
  onPrimaryContainer: Color(0xFF4A3305),
  secondary: Color(0xFF1B7A4B),
  onSecondary: Color(0xFFFFFFFF),
  secondaryContainer: Color(0xFFD3EFDD),
  onSecondaryContainer: Color(0xFF0B3D24),
  surface: Color(0xFFFFFFFF),
  onSurface: Color(0xFF1A1C1A),
  surfaceContainerHighest: Color(0xFFEEF0EC),
  onSurfaceVariant: Color(0xFF5B635D),
  outline: Color(0xFF8A928C),
  outlineVariant: Color(0xFFDADFDA),
  error: Color(0xFFBA1A1A),
  onError: Color(0xFFFFFFFF),
  errorContainer: Color(0xFFFFDAD6),
  onErrorContainer: Color(0xFF410002),
  inverseSurface: Color(0xFF2E312E),
  onInverseSurface: Color(0xFFF1F2EE),
  inversePrimary: Color(0xFFF2C46B),
  scrim: Color(0x801A1C1A),
);

/// Tokens que Material 3 no tiene: `ods-12`, avisos, estados, deshabilitados y
/// matices de tinta (`implementacion.md`).
@immutable
class RtExtraColors extends ThemeExtension<RtExtraColors> {
  const RtExtraColors({
    this.ods12 = const Color(0xFFBF8B2E),
    this.primaryPressed = const Color(0xFF6E4E10),
    this.secondaryPressed = const Color(0xFF135C38),
    this.inkSubtle = const Color(0xFF6B736D),
    this.warningContainer = const Color(0xFFFFF1D6),
    this.onWarningContainer = const Color(0xFF8A5300),
    this.infoContainer = const Color(0xFFDCE9FF),
    this.onInfoContainer = const Color(0xFF1F4E99),
    this.statusPendingBg = const Color(0xFFFFF1D6),
    this.statusPendingInk = const Color(0xFF8A5300),
    this.statusConfirmedBg = const Color(0xFFDCE9FF),
    this.statusConfirmedInk = const Color(0xFF1F4E99),
    this.statusCollectedBg = const Color(0xFFD7F2E1),
    this.statusCollectedInk = const Color(0xFF146C3E),
    this.statusCancelledBg = const Color(0xFFECEDEB),
    this.statusCancelledInk = const Color(0xFF5B635D),
    this.statusChosenBg = const Color(0xFF1B7A4B),
    this.statusChosenInk = const Color(0xFFFFFFFF),
    this.disabledBg = const Color(0xFFE3E5E1),
    this.disabledInk = const Color(0xFF6B736D),
  });

  /// Color oficial del ODS 12.
  final Color ods12;

  /// Primario presionado y anillo de foco.
  final Color primaryPressed;

  /// Secondary presionado.
  final Color secondaryPressed;

  /// Placeholders y contadores de caracteres.
  final Color inkSubtle;

  /// Fondo de avisos de precaución.
  final Color warningContainer;

  /// Texto sobre [warningContainer].
  final Color onWarningContainer;

  /// Fondo de avisos informativos.
  final Color infoContainer;

  /// Texto sobre [infoContainer].
  final Color onInfoContainer;

  /// Etiqueta Pendiente y aceptación Esperando confirmación.
  final Color statusPendingBg;

  /// Texto de la etiqueta Pendiente.
  final Color statusPendingInk;

  /// Etiqueta Confirmado.
  final Color statusConfirmedBg;

  /// Texto de la etiqueta Confirmado.
  final Color statusConfirmedInk;

  /// Etiqueta Recolectado.
  final Color statusCollectedBg;

  /// Texto de la etiqueta Recolectado.
  final Color statusCollectedInk;

  /// Etiquetas Cancelado, Cancelado por el vecino y No elegido.
  final Color statusCancelledBg;

  /// Texto de las etiquetas grises.
  final Color statusCancelledInk;

  /// Etiqueta Elegido (relleno sólido).
  final Color statusChosenBg;

  /// Texto de la etiqueta Elegido.
  final Color statusChosenInk;

  /// Fondo de controles deshabilitados.
  final Color disabledBg;

  /// Texto de controles deshabilitados.
  final Color disabledInk;

  @override
  RtExtraColors copyWith({
    Color? ods12,
    Color? primaryPressed,
    Color? secondaryPressed,
    Color? inkSubtle,
    Color? warningContainer,
    Color? onWarningContainer,
    Color? infoContainer,
    Color? onInfoContainer,
    Color? statusPendingBg,
    Color? statusPendingInk,
    Color? statusConfirmedBg,
    Color? statusConfirmedInk,
    Color? statusCollectedBg,
    Color? statusCollectedInk,
    Color? statusCancelledBg,
    Color? statusCancelledInk,
    Color? statusChosenBg,
    Color? statusChosenInk,
    Color? disabledBg,
    Color? disabledInk,
  }) {
    return RtExtraColors(
      ods12: ods12 ?? this.ods12,
      primaryPressed: primaryPressed ?? this.primaryPressed,
      secondaryPressed: secondaryPressed ?? this.secondaryPressed,
      inkSubtle: inkSubtle ?? this.inkSubtle,
      warningContainer: warningContainer ?? this.warningContainer,
      onWarningContainer: onWarningContainer ?? this.onWarningContainer,
      infoContainer: infoContainer ?? this.infoContainer,
      onInfoContainer: onInfoContainer ?? this.onInfoContainer,
      statusPendingBg: statusPendingBg ?? this.statusPendingBg,
      statusPendingInk: statusPendingInk ?? this.statusPendingInk,
      statusConfirmedBg: statusConfirmedBg ?? this.statusConfirmedBg,
      statusConfirmedInk: statusConfirmedInk ?? this.statusConfirmedInk,
      statusCollectedBg: statusCollectedBg ?? this.statusCollectedBg,
      statusCollectedInk: statusCollectedInk ?? this.statusCollectedInk,
      statusCancelledBg: statusCancelledBg ?? this.statusCancelledBg,
      statusCancelledInk: statusCancelledInk ?? this.statusCancelledInk,
      statusChosenBg: statusChosenBg ?? this.statusChosenBg,
      statusChosenInk: statusChosenInk ?? this.statusChosenInk,
      disabledBg: disabledBg ?? this.disabledBg,
      disabledInk: disabledInk ?? this.disabledInk,
    );
  }

  @override
  RtExtraColors lerp(covariant RtExtraColors? other, double t) {
    if (other == null) {
      return this;
    }
    return RtExtraColors(
      ods12: Color.lerp(ods12, other.ods12, t)!,
      primaryPressed: Color.lerp(primaryPressed, other.primaryPressed, t)!,
      secondaryPressed: Color.lerp(
        secondaryPressed,
        other.secondaryPressed,
        t,
      )!,
      inkSubtle: Color.lerp(inkSubtle, other.inkSubtle, t)!,
      warningContainer: Color.lerp(
        warningContainer,
        other.warningContainer,
        t,
      )!,
      onWarningContainer: Color.lerp(
        onWarningContainer,
        other.onWarningContainer,
        t,
      )!,
      infoContainer: Color.lerp(infoContainer, other.infoContainer, t)!,
      onInfoContainer: Color.lerp(onInfoContainer, other.onInfoContainer, t)!,
      statusPendingBg: Color.lerp(statusPendingBg, other.statusPendingBg, t)!,
      statusPendingInk: Color.lerp(
        statusPendingInk,
        other.statusPendingInk,
        t,
      )!,
      statusConfirmedBg: Color.lerp(
        statusConfirmedBg,
        other.statusConfirmedBg,
        t,
      )!,
      statusConfirmedInk: Color.lerp(
        statusConfirmedInk,
        other.statusConfirmedInk,
        t,
      )!,
      statusCollectedBg: Color.lerp(
        statusCollectedBg,
        other.statusCollectedBg,
        t,
      )!,
      statusCollectedInk: Color.lerp(
        statusCollectedInk,
        other.statusCollectedInk,
        t,
      )!,
      statusCancelledBg: Color.lerp(
        statusCancelledBg,
        other.statusCancelledBg,
        t,
      )!,
      statusCancelledInk: Color.lerp(
        statusCancelledInk,
        other.statusCancelledInk,
        t,
      )!,
      statusChosenBg: Color.lerp(statusChosenBg, other.statusChosenBg, t)!,
      statusChosenInk: Color.lerp(statusChosenInk, other.statusChosenInk, t)!,
      disabledBg: Color.lerp(disabledBg, other.disabledBg, t)!,
      disabledInk: Color.lerp(disabledInk, other.disabledInk, t)!,
    );
  }
}
