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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.Schedule
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
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadge
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumb
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMeta
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtMetaRow
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Tarjeta de un pedido disponible para el reciclador (HU-10): zona, distancia, peso,
 * cantidad de ítems y tiempo desde la publicación. Nunca muestra dirección ni contacto.
 *
 * @param zone distrito del pedido.
 * @param distance distancia ya formateada, por ejemplo "a 1.2 km"; `null` sin ubicación.
 * @param timeAgo tiempo desde la publicación, por ejemplo "hace 2 h".
 * @param alreadyAccepted muestra la marca "Ya aceptaste".
 * @param onClick abre el detalle; `null` deja la tarjeta sin acción.
 * @param thumbnail URL de la primera foto del pedido; `null` muestra el ícono.
 */
@Composable
fun RtAvailableOrderCard(
    zone: String,
    weight: String,
    items: String,
    modifier: Modifier = Modifier,
    distance: String? = null,
    timeAgo: String? = null,
    alreadyAccepted: Boolean = false,
    onClick: (() -> Unit)? = null,
    thumbnail: Any? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(RtSpacing.s3),
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RtPhotoThumb(model = thumbnail)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
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
                if (alreadyAccepted) {
                    RtBadge(text = "Ya aceptaste", icon = Icons.Rounded.Check)
                }
            }
            RtMetaRow(
                items = listOfNotNull(
                    distance?.let { RtMeta(Icons.Rounded.NearMe, it) },
                    RtMeta(Icons.Rounded.Scale, weight),
                    RtMeta(Icons.Rounded.Inventory2, items),
                ),
            )
            if (timeAgo != null) {
                RtMetaRow(items = listOf(RtMeta(Icons.Rounded.Schedule, timeAgo)))
            }
        }
    }
}

@Preview(name = "Con distancia, ya aceptado y sin ubicación", showBackground = true)
@Composable
private fun RtAvailableOrderCardPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtAvailableOrderCard(
                    zone = "Víctor Larco Herrera",
                    weight = "14.5 kg",
                    items = "3 ítems",
                    distance = "a 1.2 km",
                    timeAgo = "hace 2 h",
                    onClick = {},
                )
                RtAvailableOrderCard(
                    zone = "Trujillo",
                    weight = "4 kg",
                    items = "1 ítem",
                    distance = "a 2.6 km",
                    timeAgo = "hace 20 min",
                    alreadyAccepted = true,
                    onClick = {},
                )
                RtAvailableOrderCard(
                    zone = "La Esperanza",
                    weight = "22 kg",
                    items = "4 ítems",
                    timeAgo = "ayer",
                )
            }
        }
    }
}
