package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadge
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadgeVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Pestaña de [RtTabs].
 *
 * @param count número junto al nombre, por ejemplo los pedidos activos; `null` lo oculta.
 */
data class RtTab(
    val label: String,
    val count: Int? = null,
)

/**
 * Pestañas de una lista, por ejemplo Activos e Historial. La pestaña activa lleva la
 * línea `primary` debajo.
 */
@Composable
fun RtTabs(
    tabs: List<RtTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth().background(colorScheme.surface)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onSelect(index) },
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.height(RtSize.touchTarget),
                        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = tab.label,
                            style = RtTextStyles.bodyStrong,
                            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                        )
                        if (tab.count != null && tab.count > 0) {
                            RtBadge(text = tab.count.toString(), variant = RtBadgeVariant.Count)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(RtStroke.focus)
                            .background(if (isSelected) colorScheme.primary else colorScheme.surface),
                    )
                }
            }
        }
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outline)
    }
}

@Preview(name = "Activos e Historial", showBackground = true)
@Composable
private fun RtTabsPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtTabs(
                tabs = listOf(RtTab("Activos", count = 3), RtTab("Historial")),
                selectedIndex = 0,
                onSelect = {},
            )
        }
    }
}
