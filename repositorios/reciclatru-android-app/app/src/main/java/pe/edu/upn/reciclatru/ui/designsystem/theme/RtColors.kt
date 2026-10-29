package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color del sistema de diseño mapeados a Material 3.
 * Fuente: `tokens.json` y `implementacion.md` del sistema de diseño.
 */
internal val RtLightColorScheme = lightColorScheme(
    primary = Color(0xFF8C6418),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF6E7C8),
    onPrimaryContainer = Color(0xFF4A3305),
    secondary = Color(0xFF1B7A4B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD3EFDD),
    onSecondaryContainer = Color(0xFF0B3D24),
    background = Color(0xFFF7F8F5),
    onBackground = Color(0xFF1A1C1A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1A),
    surfaceVariant = Color(0xFFEEF0EC),
    onSurfaceVariant = Color(0xFF5B635D),
    outline = Color(0xFF8A928C),
    outlineVariant = Color(0xFFDADFDA),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    inverseSurface = Color(0xFF2E312E),
    inverseOnSurface = Color(0xFFF1F2EE),
    inversePrimary = Color(0xFFF2C46B),
    scrim = Color(0x801A1C1A),
)

/**
 * Tokens que Material 3 no tiene: identidad ODS 12, estados del pedido, avisos,
 * deshabilitados y variantes presionadas.
 */
@Immutable
data class RtExtraColors(
    val ods12: Color = Color(0xFFBF8B2E),
    val primaryPressed: Color = Color(0xFF6E4E10),
    val secondaryPressed: Color = Color(0xFF135C38),
    val inkSubtle: Color = Color(0xFF6B736D),
    val warningContainer: Color = Color(0xFFFFF1D6),
    val onWarningContainer: Color = Color(0xFF8A5300),
    val infoContainer: Color = Color(0xFFDCE9FF),
    val onInfoContainer: Color = Color(0xFF1F4E99),
    val statusPendingBg: Color = Color(0xFFFFF1D6),
    val statusPendingInk: Color = Color(0xFF8A5300),
    val statusConfirmedBg: Color = Color(0xFFDCE9FF),
    val statusConfirmedInk: Color = Color(0xFF1F4E99),
    val statusCollectedBg: Color = Color(0xFFD7F2E1),
    val statusCollectedInk: Color = Color(0xFF146C3E),
    val statusCancelledBg: Color = Color(0xFFECEDEB),
    val statusCancelledInk: Color = Color(0xFF5B635D),
    val statusChosenBg: Color = Color(0xFF1B7A4B),
    val statusChosenInk: Color = Color(0xFFFFFFFF),
    val disabledBg: Color = Color(0xFFE3E5E1),
    val disabledInk: Color = Color(0xFF6B736D),
)

val LocalRtColors = staticCompositionLocalOf { RtExtraColors() }

/** Acceso a los tokens propios desde cualquier composable dentro de [RtTheme]. */
val MaterialTheme.rtColors: RtExtraColors
    @Composable
    @ReadOnlyComposable
    get() = LocalRtColors.current
