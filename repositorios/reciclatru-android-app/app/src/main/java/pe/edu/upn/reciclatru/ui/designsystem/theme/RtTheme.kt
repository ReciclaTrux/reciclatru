package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Tema único de la app: tokens del sistema de diseño aplicados sobre Material 3.
 * No hay tema oscuro ni color dinámico: el sistema define solo el tema claro.
 */
@Composable
fun RtTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalRtColors provides RtExtraColors()) {
        MaterialTheme(
            colorScheme = RtLightColorScheme,
            typography = RtTypography,
            shapes = RtShapes,
            content = content,
        )
    }
}
