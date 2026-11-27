package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceChip
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAcceptanceState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMeta
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMetaRow
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/**
 * Tarjeta de un pedido aceptado por el reciclador en Mis aceptados (HU-14): zona, peso,
 * fecha de aceptación y estado. Cuando el vecino lo eligió se resalta con borde y la acción
 * "Ir a recoger".
 *
 * @param zone distrito del pedido.
 * @param weight peso total ya formateado, por ejemplo "14.5 kg".
 * @param acceptedAt fecha de aceptación ya formateada, por ejemplo "Aceptado hoy, 9:15".
 * @param onClick abre el detalle o la pantalla de recojo.
 */
@Composable
fun RtAcceptedOrderCard(
    zone: String,
    weight: String,
    acceptedAt: String,
    state: RtAcceptanceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val shape = RoundedCornerShape(RtRadius.lg)
    val chosen = state == RtAcceptanceState.Chosen

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(
                width = if (chosen) RtStroke.focus else RtStroke.divider,
                color = if (chosen) rtColors.statusChosenBg else colorScheme.outlineVariant,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(RtSpacing.s3),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = zone,
                style = MaterialTheme.typography.titleMedium,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            RtAcceptanceChip(state = state)
        }
        RtMetaRow(
            items = listOf(
                RtMeta(Icons.Rounded.Scale, weight),
                RtMeta(Icons.Rounded.Schedule, acceptedAt),
            ),
        )
        if (chosen) {
            HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outlineVariant)
            Row(
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconSm),
                    tint = rtColors.statusChosenBg,
                )
                Text(
                    text = "Te eligieron. Ir a recoger",
                    style = RtTextStyles.bodyStrong,
                    color = rtColors.statusChosenBg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = rtColors.statusChosenBg,
                )
            }
        }
    }
}

@Preview(name = "Elegido, esperando, no elegido y recolectado", showBackground = true)
@Composable
private fun RtAcceptedOrderCardPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtAcceptedOrderCard(
                    zone = "Víctor Larco Herrera",
                    weight = "14.5 kg",
                    acceptedAt = "Aceptado hoy, 9:15",
                    state = RtAcceptanceState.Chosen,
                    onClick = {},
                )
                RtAcceptedOrderCard(
                    zone = "Trujillo",
                    weight = "4 kg",
                    acceptedAt = "Aceptado ayer, 18:40",
                    state = RtAcceptanceState.Waiting,
                    onClick = {},
                )
                RtAcceptedOrderCard(
                    zone = "La Esperanza",
                    weight = "22 kg",
                    acceptedAt = "Aceptado 12 nov",
                    state = RtAcceptanceState.NotChosen,
                    onClick = {},
                )
                RtAcceptedOrderCard(
                    zone = "El Porvenir",
                    weight = "8 kg",
                    acceptedAt = "Aceptado 10 nov",
                    state = RtAcceptanceState.Collected,
                    onClick = {},
                )
            }
        }
    }
}
