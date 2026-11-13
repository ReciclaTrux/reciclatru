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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Estado vacío de una lista: ícono, qué pasa y qué puede hacer la persona.
 *
 * @param title qué no hay todavía, por ejemplo "Aún no publicaste pedidos".
 * @param message qué hacer o qué esperar.
 * @param actionLabel acción principal opcional; requiere [onAction].
 */
@Composable
fun RtEmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null,
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
                .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(RtSize.stateIconGlyph),
                tint = colorScheme.onPrimaryContainer,
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
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = RtSize.stateMessageMaxWidth),
        )
        if (actionLabel != null && onAction != null) {
            RtButton(
                text = actionLabel,
                onClick = onAction,
                icon = actionIcon,
                modifier = Modifier.padding(top = RtSpacing.s3),
            )
        }
    }
}

@Preview(name = "Con acción", showBackground = true)
@Composable
private fun RtEmptyStatePreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtEmptyState(
                icon = Icons.Rounded.Recycling,
                title = "Aún no publicaste pedidos",
                message = "Publica el material que tienes y un reciclador de tu zona lo recogerá.",
                actionLabel = "Publicar pedido",
                actionIcon = Icons.Rounded.Add,
                onAction = {},
            )
        }
    }
}
