package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import pe.edu.upn.reciclatru.R

private val RtGoogleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val NunitoSans = GoogleFont("Nunito Sans")

/** Nunito Sans (400, 600 y 700) descargada con el proveedor de Google Fonts. */
val RtFontFamily = FontFamily(
    Font(googleFont = NunitoSans, fontProvider = RtGoogleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = NunitoSans, fontProvider = RtGoogleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = NunitoSans, fontProvider = RtGoogleFontProvider, weight = FontWeight.Bold),
)

/**
 * Escala tipográfica mapeada a Material 3 según `implementacion.md`.
 * `headline-lg` → `headlineMedium`, `headline` → `headlineSmall`,
 * `title` → `titleLarge`, `subtitle` → `titleMedium`, `body` → `bodyLarge`,
 * `body-sm` → `bodyMedium`, `label` → `labelLarge`, `caption` → `labelMedium`.
 */
val RtTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
)

/** Estilos del sistema que Material 3 no tiene como rol propio. */
object RtTextStyles {
    /** `body-strong` 16/24 600: nombres y datos que se ubican rápido. */
    val bodyStrong = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    /** `button` 16/24 700. */
    val button = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )

    /** `button` en tamaño compacto (40 px) de la guía de RtButton. */
    val buttonSmall = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    )

    /** Iniciales de RtAvatar: 15/1 700 según la guía del componente. */
    val avatarInitials = TextStyle(
        fontFamily = RtFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 15.sp,
    )
}
