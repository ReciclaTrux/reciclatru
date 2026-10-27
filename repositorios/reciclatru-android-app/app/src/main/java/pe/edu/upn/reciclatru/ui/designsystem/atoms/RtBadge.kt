package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Variantes de [RtBadge]: marca, neutra y contador. */
enum class RtBadgeVariant { Brand, Neutral, Count }

/**
 * Marca pequeña de información que no es un estado del pedido.
 *
 * @param icon ícono opcional antes del texto.
 * @param variant [RtBadgeVariant.Brand] para "Ya aceptaste",
 *   [RtBadgeVariant.Neutral] para aceptaciones o datos secundarios,
 *   [RtBadgeVariant.Count] para un número.
 */
@Composable
fun RtBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    variant: RtBadgeVariant = RtBadgeVariant.Brand,
) {
    val containerColor = when (variant) {
        RtBadgeVariant.Brand -> MaterialTheme.colorScheme.primaryContainer
        RtBadgeVariant.Neutral -> MaterialTheme.colorScheme.surfaceVariant
        RtBadgeVariant.Count -> MaterialTheme.colorScheme.primary
    }
    val contentColor = when (variant) {
        RtBadgeVariant.Brand -> MaterialTheme.colorScheme.onPrimaryContainer
        RtBadgeVariant.Neutral -> MaterialTheme.colorScheme.onSurfaceVariant
        RtBadgeVariant.Count -> MaterialTheme.colorScheme.onPrimary
    }
    val isCount = variant == RtBadgeVariant.Count

    Row(
        modifier = modifier
            .height(RtSize.badgeHeight)
            .then(if (isCount) Modifier.defaultMinSize(minWidth = RtSize.badgeHeight) else Modifier)
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = RtSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCount) Arrangement.Center else Arrangement.spacedBy(RtSpacing.s1),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = Modifier.size(RtSize.iconCaption),
                tint = contentColor,
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Preview(name = "Marca, neutra y contador", showBackground = true)
@Composable
private fun RtBadgePreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(RtSpacing.s4),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RtBadge(text = "Ya aceptaste", icon = Icons.Rounded.Check)
                RtBadge(
                    text = "3 aceptaciones",
                    icon = Icons.Rounded.Group,
                    variant = RtBadgeVariant.Neutral,
                )
                RtBadge(text = "Sin aceptaciones aún", variant = RtBadgeVariant.Neutral)
                RtBadge(text = "3", variant = RtBadgeVariant.Count)
            }
        }
    }
}
