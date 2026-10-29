package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Tamaños de [RtSpinner]: grande para la carga inicial, pequeño dentro de un control. */
enum class RtSpinnerSize { Small, Large }

/**
 * Indicador circular de carga indeterminada.
 *
 * @param size [RtSpinnerSize.Large] (32 px, `primary`) para la primera carga de una
 *   lista o detalle; [RtSpinnerSize.Small] (20 px, color del texto) dentro de un botón
 *   o sobre una foto que se sube.
 * @param color color del arco; por defecto, el color de contenido actual.
 */
@Composable
fun RtSpinner(
    modifier: Modifier = Modifier,
    size: RtSpinnerSize = RtSpinnerSize.Small,
    color: Color = LocalContentColor.current,
) {
    val isLarge = size == RtSpinnerSize.Large
    CircularProgressIndicator(
        modifier = modifier
            .size(if (isLarge) RtSize.spinnerLarge else RtSize.spinnerSmall)
            .semantics { contentDescription = "Cargando" },
        color = color,
        strokeWidth = if (isLarge) RtStroke.spinnerLarge else RtStroke.spinnerSmall,
        trackColor = Color.Transparent,
    )
}

@Preview(name = "Grande y pequeño", showBackground = true)
@Composable
private fun RtSpinnerPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(RtSpacing.s4),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RtSpinner(size = RtSpinnerSize.Large, color = MaterialTheme.colorScheme.primary)
                RtSpinner(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
