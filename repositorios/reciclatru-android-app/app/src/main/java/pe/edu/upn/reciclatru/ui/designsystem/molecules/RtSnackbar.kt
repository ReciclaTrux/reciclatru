package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtElevation
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Mensaje breve en la parte inferior que confirma una acción o avisa un fallo
 * recuperable (HU-03, HU-05, HU-07, HU-09, HU-15). Una línea, sin "exitosamente" ni
 * signos de exclamación.
 *
 * @param actionLabel acción opcional, por ejemplo "Reintentar" o "Ver".
 */
@Composable
fun RtSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.md)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = RtElevation.level2, shape = shape, clip = false)
            .clip(shape)
            .background(colorScheme.inverseSurface)
            .defaultMinSize(minHeight = RtSize.touchTarget)
            .padding(
                start = RtSpacing.s4,
                end = RtSpacing.s2,
                top = RtSpacing.s2,
                bottom = RtSpacing.s2,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.inverseOnSurface,
        )
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                modifier = Modifier.height(RtSize.buttonHeightSmall),
                contentPadding = PaddingValues(horizontal = RtSpacing.s3),
            ) {
                Text(
                    text = actionLabel,
                    style = RtTextStyles.button,
                    color = colorScheme.inversePrimary,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Host del snackbar para el `Scaffold` de la pantalla: dibuja [RtSnackbar] con el
 * mensaje y la acción que reciba de [SnackbarHostState]. Sin acción dura 4 segundos y
 * con acción 8; usa `duration` al llamar a `showSnackbar`.
 */
@Composable
fun RtSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        RtSnackbar(
            message = data.visuals.message,
            actionLabel = data.visuals.actionLabel,
            onAction = if (data.visuals.actionLabel != null) data::performAction else null,
        )
    }
}

@Preview(name = "Con y sin acción", showBackground = true)
@Composable
private fun RtSnackbarPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtSnackbar(message = "Pedido publicado", actionLabel = "Ver", onAction = {})
                RtSnackbar(
                    message = "No se pudo subir una foto",
                    actionLabel = "Reintentar",
                    onAction = {},
                )
                RtSnackbar(message = "Cambios guardados")
            }
        }
    }
}
