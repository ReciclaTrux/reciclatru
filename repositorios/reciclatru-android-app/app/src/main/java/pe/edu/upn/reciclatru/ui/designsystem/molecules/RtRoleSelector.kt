package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Recycling
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
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Rol que se elige al registrarse; se mantiene durante toda la cuenta. */
enum class RtRole { Neighbor, Recycler }

/**
 * Dos opciones grandes para elegir el rol al registrarse (HU-01).
 *
 * @param selected rol marcado; `null` al abrir la pantalla, el usuario debe elegir a
 *   propósito.
 * @param error mensaje que aparece si se intenta continuar sin rol; pinta el borde de
 *   ambas opciones en `error`. La ayuda "No podrás cambiar el rol después" sigue visible.
 */
@Composable
fun RtRoleSelector(
    selected: RtRole?,
    onSelect: (RtRole) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        Text(
            text = "¿Cómo usarás ReciclaTru?",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurface,
        )
        RtRoleOption(
            icon = Icons.Rounded.Home,
            title = "Soy vecino",
            description = "Publico el material que quiero entregar",
            selected = selected == RtRole.Neighbor,
            hasError = error != null,
            onClick = { onSelect(RtRole.Neighbor) },
        )
        RtRoleOption(
            icon = Icons.Rounded.Recycling,
            title = "Soy reciclador",
            description = "Recojo material cerca de mí",
            selected = selected == RtRole.Recycler,
            hasError = error != null,
            onClick = { onSelect(RtRole.Recycler) },
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                modifier = Modifier.size(RtSize.iconCaption),
                tint = colorScheme.onSurfaceVariant,
            )
            Text(
                text = "No podrás cambiar el rol después",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        if (error != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconCaption),
                    tint = colorScheme.error,
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun RtRoleOption(
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    hasError: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.md)
    val borderColor = when {
        hasError -> colorScheme.error
        selected -> colorScheme.primary
        else -> colorScheme.outline
    }
    val borderWidth = if (selected) RtStroke.roleSelected else RtStroke.control

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) colorScheme.primaryContainer else colorScheme.surface)
            .border(borderWidth, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(RtSpacing.s4),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Box(
            modifier = Modifier
                .size(RtSize.touchTarget)
                .clip(CircleShape)
                .background(if (selected) colorScheme.primary else colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(RtSize.iconMd),
                tint = if (selected) colorScheme.onPrimary else colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .size(RtSize.roleRadio)
                .clip(CircleShape)
                .background(if (selected) colorScheme.surface else Color.Transparent)
                .border(
                    width = if (selected) RtStroke.roleRadioSelected else RtStroke.roleRadio,
                    color = if (selected) colorScheme.primary else colorScheme.outline,
                    shape = CircleShape,
                ),
        )
    }
}

@Preview(name = "Sin selección", showBackground = true, heightDp = 340)
@Composable
private fun RtRoleSelectorEmptyPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(RtSpacing.s4)) {
                RtRoleSelector(selected = null, onSelect = {})
            }
        }
    }
}

@Preview(name = "Vecino seleccionado", showBackground = true, heightDp = 340)
@Composable
private fun RtRoleSelectorSelectedPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(RtSpacing.s4)) {
                RtRoleSelector(selected = RtRole.Neighbor, onSelect = {})
            }
        }
    }
}

@Preview(name = "Error sin rol", showBackground = true, heightDp = 380)
@Composable
private fun RtRoleSelectorErrorPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(RtSpacing.s4)) {
                RtRoleSelector(
                    selected = null,
                    onSelect = {},
                    error = "Elige si eres vecino o reciclador",
                )
            }
        }
    }
}
