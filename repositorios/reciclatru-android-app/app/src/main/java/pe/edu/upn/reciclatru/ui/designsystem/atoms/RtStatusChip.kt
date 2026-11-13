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
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/** Estados de un pedido que muestra [RtStatusChip]. */
enum class RtOrderStatus { Pending, Confirmed, Collected, Cancelled }

/**
 * Etiqueta con el estado del pedido: Pendiente, Confirmado, Recolectado o Cancelado.
 * El estado se distingue por ícono y texto, no solo por color.
 */
@Composable
fun RtStatusChip(
    status: RtOrderStatus,
    modifier: Modifier = Modifier,
) {
    val rtColors = MaterialTheme.rtColors
    val containerColor = when (status) {
        RtOrderStatus.Pending -> rtColors.statusPendingBg
        RtOrderStatus.Confirmed -> rtColors.statusConfirmedBg
        RtOrderStatus.Collected -> rtColors.statusCollectedBg
        RtOrderStatus.Cancelled -> rtColors.statusCancelledBg
    }
    val contentColor = when (status) {
        RtOrderStatus.Pending -> rtColors.statusPendingInk
        RtOrderStatus.Confirmed -> rtColors.statusConfirmedInk
        RtOrderStatus.Collected -> rtColors.statusCollectedInk
        RtOrderStatus.Cancelled -> rtColors.statusCancelledInk
    }
    val icon = when (status) {
        RtOrderStatus.Pending -> Icons.Rounded.Schedule
        RtOrderStatus.Confirmed -> Icons.Rounded.HowToReg
        RtOrderStatus.Collected -> Icons.Rounded.Recycling
        RtOrderStatus.Cancelled -> Icons.Rounded.Block
    }
    val label = when (status) {
        RtOrderStatus.Pending -> "Pendiente"
        RtOrderStatus.Confirmed -> "Confirmado"
        RtOrderStatus.Collected -> "Recolectado"
        RtOrderStatus.Cancelled -> "Cancelado"
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

@Preview(name = "Los cuatro estados", showBackground = true)
@Composable
private fun RtStatusChipPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            ) {
                RtOrderStatus.entries.forEach { status -> RtStatusChip(status = status) }
            }
        }
    }
}
