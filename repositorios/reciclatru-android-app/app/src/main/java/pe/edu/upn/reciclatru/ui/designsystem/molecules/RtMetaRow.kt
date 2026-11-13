package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Dato corto de un pedido para [RtMetaRow]: ícono y texto, por ejemplo "3 ítems". */
data class RtMeta(
    val icon: ImageVector,
    val text: String,
)

/**
 * Fila de datos cortos del pedido, cada uno con su ícono: cantidad de ítems, peso,
 * distancia o fecha.
 */
@Composable
fun RtMetaRow(
    items: List<RtMeta>,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconCaption),
                    tint = color,
                )
                Text(
                    text = item.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(name = "Ítems y peso", showBackground = true)
@Composable
private fun RtMetaRowPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtMetaRow(
                items = listOf(
                    RtMeta(Icons.Rounded.Inventory2, "3 ítems"),
                    RtMeta(Icons.Rounded.Scale, "14.5 kg"),
                ),
                modifier = Modifier.padding(RtSpacing.s4),
            )
        }
    }
}
