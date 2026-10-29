package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Motivo del error de carga: sin conexión o falla del servidor. */
enum class RtErrorKind { NoConnection, ServerError }

/**
 * Pantalla de error de carga con Reintentar; la piden todas las listas y detalles del
 * Release 1. Si ya había datos en pantalla no la reemplaces: usa [RtSnackbar].
 *
 * @param title qué no se pudo cargar, por ejemplo "No pudimos cargar tus pedidos".
 * @param message qué revisar, por ejemplo "Revisa tu conexión a internet e inténtalo de
 *   nuevo.".
 * @param kind sin conexión (`wifi_off`) o error del servidor (`cloud_off`).
 */
@Composable
fun RtErrorState(
    title: String,
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    kind: RtErrorKind = RtErrorKind.NoConnection,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = RtSpacing.s5, vertical = RtSpacing.s7),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = RtSpacing.s2)
                .size(RtSize.stateIcon)
                .clip(CircleShape)
                .background(colorScheme.errorContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (kind == RtErrorKind.NoConnection) {
                    Icons.Rounded.WifiOff
                } else {
                    Icons.Rounded.CloudOff
                },
                contentDescription = null,
                modifier = Modifier.size(RtSize.stateIconGlyph),
                tint = colorScheme.onErrorContainer,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            modifier = Modifier.widthIn(max = RtSize.stateMessageMaxWidth),
            style = MaterialTheme.typography.bodyLarge,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        RtButton(
            text = "Reintentar",
            onClick = onRetry,
            modifier = Modifier.padding(top = RtSpacing.s3),
            variant = RtButtonVariant.Secondary,
            icon = Icons.Rounded.Refresh,
        )
    }
}

@Preview(name = "Sin conexión", showBackground = true, heightDp = 380)
@Composable
private fun RtErrorStateNoConnectionPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtErrorState(
                title = "No pudimos cargar tus pedidos",
                message = "Revisa tu conexión a internet e inténtalo de nuevo.",
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Error del servidor", showBackground = true, heightDp = 380)
@Composable
private fun RtErrorStateServerPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtErrorState(
                title = "No pudimos cargar el mapa",
                message = "El servicio no respondió. Inténtalo de nuevo en un momento.",
                onRetry = {},
                kind = RtErrorKind.ServerError,
            )
        }
    }
}
