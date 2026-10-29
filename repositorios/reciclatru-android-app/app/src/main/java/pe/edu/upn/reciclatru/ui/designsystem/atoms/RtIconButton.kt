package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Variantes de [RtIconButton] según el uso de la acción. */
enum class RtIconButtonVariant { Standard, Tonal, Ink }

/**
 * Botón circular de 48 px con un solo ícono. Siempre necesita descripción accesible.
 *
 * @param contentDescription descripción de la acción, por ejemplo "Llamar a Juan Pérez".
 * @param variant [RtIconButtonVariant.Standard] para acciones secundarias repetidas,
 *   [RtIconButtonVariant.Tonal] para llamar o cómo llegar, [RtIconButtonVariant.Ink]
 *   para volver sobre un fondo claro.
 */
@Composable
fun RtIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RtIconButtonVariant = RtIconButtonVariant.Standard,
) {
    val containerColor = when (variant) {
        RtIconButtonVariant.Tonal -> MaterialTheme.colorScheme.secondaryContainer
        RtIconButtonVariant.Standard, RtIconButtonVariant.Ink -> Color.Transparent
    }
    val contentColor = when (variant) {
        RtIconButtonVariant.Tonal -> MaterialTheme.colorScheme.onSecondaryContainer
        RtIconButtonVariant.Standard -> MaterialTheme.colorScheme.onSurfaceVariant
        RtIconButtonVariant.Ink -> MaterialTheme.colorScheme.onSurface
    }

    IconButton(
        onClick = onClick,
        modifier = modifier.size(RtSize.touchTarget),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(RtSize.iconMd),
        )
    }
}

@Preview(name = "Estándar, tinta y tonal", showBackground = true)
@Composable
private fun RtIconButtonPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(RtSpacing.s4),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RtIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    onClick = {},
                    variant = RtIconButtonVariant.Ink,
                )
                RtIconButton(
                    icon = Icons.Rounded.Visibility,
                    contentDescription = "Mostrar contraseña",
                    onClick = {},
                )
                RtIconButton(
                    icon = Icons.Rounded.Delete,
                    contentDescription = "Quitar ítem",
                    onClick = {},
                )
                RtIconButton(
                    icon = Icons.Rounded.ContentCopy,
                    contentDescription = "Copiar dirección",
                    onClick = {},
                )
                RtIconButton(
                    icon = Icons.Rounded.Call,
                    contentDescription = "Llamar",
                    onClick = {},
                    variant = RtIconButtonVariant.Tonal,
                )
                RtIconButton(
                    icon = Icons.Rounded.Directions,
                    contentDescription = "Cómo llegar",
                    onClick = {},
                    variant = RtIconButtonVariant.Tonal,
                )
            }
        }
    }
}
