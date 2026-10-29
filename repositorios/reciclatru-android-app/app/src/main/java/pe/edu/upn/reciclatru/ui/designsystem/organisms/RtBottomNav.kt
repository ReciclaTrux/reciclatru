package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Destino de [RtBottomNav].
 *
 * @param route identificador de la pantalla; se compara con `selected`.
 */
data class RtBottomNavItem(
    val icon: ImageVector,
    val label: String,
    val route: String,
)

/**
 * Navegación principal de tres destinos, distinta para cada rol. Vecino: Mis pedidos,
 * Publicar, Perfil. Reciclador: Disponibles, Aceptados, Perfil. Cerrar sesión vive en
 * Perfil, no en la barra.
 *
 * @param selected ruta del destino activo: lo marca con la píldora `primary-container`.
 */
@Composable
fun RtBottomNav(
    items: List<RtBottomNavItem>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth().background(colorScheme.surface)) {
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RtSize.bottomNavHeight),
        ) {
            items.forEach { item ->
                val isSelected = item.route == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelect(item.route) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = RtSize.navPillWidth, height = RtSize.navPillHeight)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) colorScheme.primaryContainer else Color.Transparent,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(RtSize.iconMd),
                            tint = if (isSelected) {
                                colorScheme.onPrimaryContainer
                            } else {
                                colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    Spacer(modifier = Modifier.height(RtSpacing.s1))
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) colorScheme.onSurface else colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

private val neighborItems = listOf(
    RtBottomNavItem(Icons.AutoMirrored.Rounded.ReceiptLong, "Mis pedidos", "orders"),
    RtBottomNavItem(Icons.Rounded.AddCircle, "Publicar", "publish"),
    RtBottomNavItem(Icons.Rounded.Person, "Perfil", "profile"),
)

private val recyclerItems = listOf(
    RtBottomNavItem(Icons.Rounded.TravelExplore, "Disponibles", "available"),
    RtBottomNavItem(Icons.Rounded.AssignmentTurnedIn, "Aceptados", "accepted"),
    RtBottomNavItem(Icons.Rounded.Person, "Perfil", "profile"),
)

@Preview(name = "Vecino y reciclador", showBackground = true, heightDp = 260)
@Composable
private fun RtBottomNavPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s4)) {
                RtBottomNav(items = neighborItems, selected = "orders", onSelect = {})
                RtBottomNav(items = recyclerItems, selected = "available", onSelect = {})
            }
        }
    }
}
