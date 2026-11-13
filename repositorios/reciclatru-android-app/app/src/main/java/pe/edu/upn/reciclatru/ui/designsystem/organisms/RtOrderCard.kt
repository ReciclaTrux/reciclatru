package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtOrderStatus
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtStatusChip
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMeta
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMetaRow
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Pie de [RtOrderCard]: quién aceptó o quién recogerá el pedido.
 *
 * @param actionLabel acción sugerida a la derecha, por ejemplo "Elegir reciclador".
 */
data class RtOrderCardFooter(
    val icon: ImageVector,
    val text: String,
    val actionLabel: String? = null,
)

/**
 * Tarjeta de un pedido del vecino en Mis pedidos (HU-06): fecha, estado, cantidad de
 * ítems y peso. Toda la tarjeta abre el detalle.
 *
 * @param title fecha de publicación ya formateada, por ejemplo "Hoy, 9:15".
 * @param items cantidad de ítems ya formateada, por ejemplo "3 ítems".
 * @param weight peso total ya formateado, por ejemplo "14.5 kg".
 * @param footer aceptaciones o reciclador elegido; `null` en el historial.
 */
@Composable
fun RtOrderCard(
    title: String,
    status: RtOrderStatus,
    items: String,
    weight: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    footer: RtOrderCardFooter? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .clickable(onClick = onClick)
            .padding(RtSpacing.s3),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(RtSize.thumbSm)
                    .clip(RoundedCornerShape(RtRadius.sm))
                    .background(colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    RtStatusChip(status = status)
                }
                RtMetaRow(
                    items = listOf(
                        RtMeta(Icons.Rounded.Inventory2, items),
                        RtMeta(Icons.Rounded.Scale, weight),
                    ),
                )
            }
        }
        if (footer != null) {
            HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outlineVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = footer.icon,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconSm),
                    tint = colorScheme.onSurfaceVariant,
                )
                Text(
                    text = footer.text,
                    style = RtTextStyles.bodyStrong,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (footer.actionLabel != null) {
                    Text(
                        text = footer.actionLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Pendiente, confirmado e historial", showBackground = true)
@Composable
private fun RtOrderCardPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtOrderCard(
                    title = "Hoy, 9:15",
                    status = RtOrderStatus.Pending,
                    items = "3 ítems",
                    weight = "14.5 kg",
                    onClick = {},
                    footer = RtOrderCardFooter(Icons.Rounded.Group, "3 aceptaciones", "Elegir reciclador"),
                )
                RtOrderCard(
                    title = "Ayer, 18:40",
                    status = RtOrderStatus.Pending,
                    items = "1 ítem",
                    weight = "5 kg",
                    onClick = {},
                    footer = RtOrderCardFooter(Icons.Rounded.Group, "Sin aceptaciones aún"),
                )
                RtOrderCard(
                    title = "10 nov",
                    status = RtOrderStatus.Confirmed,
                    items = "2 ítems",
                    weight = "9 kg",
                    onClick = {},
                    footer = RtOrderCardFooter(Icons.Rounded.Person, "Juan Pérez"),
                )
                RtOrderCard(
                    title = "3 nov",
                    status = RtOrderStatus.Collected,
                    items = "2 ítems",
                    weight = "11 kg",
                    onClick = {},
                )
            }
        }
    }
}
