package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtItemRow
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Datos de una fila de [RtItemListEditor]. */
data class RtItemListEntry(
    val description: String,
    val weight: String,
    val error: String? = null,
    val descriptionInvalid: Boolean = false,
    val weightInvalid: Boolean = false,
)

/**
 * Lista editable de ítems del pedido con el botón Agregar ítem, el contador y el peso
 * total (HU-05, HU-07). Los callbacks reciben la posición de la fila.
 *
 * @param totalWeight peso total ya formateado, por ejemplo "14.5 kg".
 * @param maxItems límite de ítems; al llegar, el botón Agregar ítem se desactiva.
 * @param error aviso de la lista completa, por ejemplo cuando no hay ítems.
 */
@Composable
fun RtItemListEditor(
    items: List<RtItemListEntry>,
    totalWeight: String,
    onDescriptionChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onRemove: (Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    maxItems: Int = 10,
    error: String? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Text(
            text = "Materiales",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurfaceVariant,
        )
        items.forEachIndexed { index, item ->
            RtItemRow(
                description = item.description,
                onDescriptionChange = { onDescriptionChange(index, it) },
                weight = item.weight,
                onWeightChange = { onWeightChange(index, it) },
                onRemove = { onRemove(index) },
                error = item.error,
                descriptionInvalid = item.descriptionInvalid,
                weightInvalid = item.weightInvalid,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RtButton(
                text = "Agregar ítem",
                onClick = onAdd,
                variant = RtButtonVariant.Text,
                icon = Icons.Rounded.Add,
                enabled = items.size < maxItems,
            )
            Text(
                text = "${items.size} de $maxItems ítems",
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RtRadius.md))
                .background(colorScheme.primaryContainer)
                .padding(horizontal = RtSpacing.s4, vertical = RtSpacing.s3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Peso total",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onPrimaryContainer,
            )
            Text(
                text = totalWeight,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Preview(name = "Con ítems", showBackground = true)
@Composable
private fun RtItemListEditorPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtItemListEditor(
                items = listOf(
                    RtItemListEntry(description = "Botellas de plástico", weight = "2.5"),
                    RtItemListEntry(description = "Cartón", weight = "12"),
                ),
                totalWeight = "14.5 kg",
                onDescriptionChange = { _, _ -> },
                onWeightChange = { _, _ -> },
                onRemove = {},
                onAdd = {},
                modifier = Modifier.padding(RtSpacing.s4),
            )
        }
    }
}

@Preview(name = "Con error y sin ítems", showBackground = true)
@Composable
private fun RtItemListEditorErrorPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
            ) {
                RtItemListEditor(
                    items = listOf(
                        RtItemListEntry(description = "Periódicos y revistas", weight = "5"),
                        RtItemListEntry(
                            description = "",
                            weight = "600",
                            error = "Escribe el material y un peso mayor a 0 y hasta 500 kg",
                            descriptionInvalid = true,
                            weightInvalid = true,
                        ),
                    ),
                    totalWeight = "5 kg",
                    onDescriptionChange = { _, _ -> },
                    onWeightChange = { _, _ -> },
                    onRemove = {},
                    onAdd = {},
                )
                RtItemListEditor(
                    items = emptyList(),
                    totalWeight = "0 kg",
                    onDescriptionChange = { _, _ -> },
                    onWeightChange = { _, _ -> },
                    onRemove = {},
                    onAdd = {},
                    error = "Agrega al menos un ítem para publicar",
                )
            }
        }
    }
}
