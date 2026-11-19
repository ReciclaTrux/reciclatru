package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/** Estados de una aceptación desde el punto de vista del reciclador. */
enum class RtAcceptanceState { Waiting, Chosen, Collected, CancelledByNeighbor, NotChosen }

/**
 * Etiqueta con el estado de una aceptación del reciclador (HU-12, HU-14, HU-15): esperando
 * confirmación, elegido, recolectado, cancelado por el vecino o no elegido. Se distingue por
 * ícono y texto, no solo por color.
 */
@Composable
fun RtAcceptanceChip(
    state: RtAcceptanceState,
    modifier: Modifier = Modifier,
) {
    val rtColors = MaterialTheme.rtColors
    val containerColor: Color
    val contentColor: Color
    when (state) {
        RtAcceptanceState.Waiting -> {
            containerColor = rtColors.statusPendingBg
            contentColor = rtColors.statusPendingInk
        }
        RtAcceptanceState.Chosen -> {
            containerColor = rtColors.statusChosenBg
            contentColor = rtColors.statusChosenInk
        }
        RtAcceptanceState.Collected -> {
            containerColor = rtColors.statusCollectedBg
            contentColor = rtColors.statusCollectedInk
        }
        RtAcceptanceState.CancelledByNeighbor, RtAcceptanceState.NotChosen -> {
            containerColor = rtColors.statusCancelledBg
            contentColor = rtColors.statusCancelledInk
        }
    }
    val icon = when (state) {
        RtAcceptanceState.Waiting -> Icons.Rounded.HourglassTop
        RtAcceptanceState.Chosen -> Icons.Rounded.Verified
        RtAcceptanceState.Collected -> Icons.Rounded.Recycling
        RtAcceptanceState.CancelledByNeighbor -> Icons.Rounded.Block
        RtAcceptanceState.NotChosen -> Icons.Rounded.PersonOff
    }
    val label = when (state) {
        RtAcceptanceState.Waiting -> "Esperando confirmación"
        RtAcceptanceState.Chosen -> "Elegido"
        RtAcceptanceState.Collected -> "Recolectado"
        RtAcceptanceState.CancelledByNeighbor -> "Cancelado por el vecino"
        RtAcceptanceState.NotChosen -> "No elegido"
    }

    Row(
        modifier = modifier
            .height(RtSize.badgeHeight)
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = RtSpacing.s2),
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(RtSize.iconCaption),
            tint = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Preview(name = "Los cinco estados", showBackground = true)
@Composable
private fun RtAcceptanceChipPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            ) {
                RtAcceptanceState.entries.forEach { state -> RtAcceptanceChip(state = state) }
            }
        }
    }
}
