package pe.edu.upn.reciclatru.view.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import pe.edu.upn.reciclatru.model.orders.OrderItem
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles

/** Tarjeta "Materiales" con los ítems y el peso total; la comparten el detalle del vecino y el del reciclador. */
@Composable
internal fun MaterialsCard(
    items: List<OrderItem>,
    totalWeightKg: Double,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s4)
            .testTag("card_materiales"),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Text(
            text = "Materiales",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
        )
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatWeightKg(item.weightKg),
                    style = RtTextStyles.bodyStrong,
                    color = colorScheme.onSurface,
                )
            }
        }
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outlineVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
            Text(
                text = "Peso total",
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = formatWeightKg(totalWeightKg),
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
            )
        }
    }
}
