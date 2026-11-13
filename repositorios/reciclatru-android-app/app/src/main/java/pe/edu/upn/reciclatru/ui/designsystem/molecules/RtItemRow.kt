package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Fila de un ítem del pedido: material, peso en kg y botón para quitarlo (HU-05, HU-07).
 *
 * @param error texto que dice qué corregir en la fila; va debajo de los dos campos.
 * @param descriptionInvalid marca en rojo el campo Material.
 * @param weightInvalid marca en rojo el campo Peso.
 */
@Composable
fun RtItemRow(
    description: String,
    onDescriptionChange: (String) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    descriptionInvalid: Boolean = false,
    weightInvalid: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            verticalAlignment = Alignment.Bottom,
        ) {
            RtTextField(
                label = "Material",
                value = description,
                onValueChange = onDescriptionChange,
                modifier = Modifier.weight(DescriptionWeight),
                placeholder = "Botellas de plástico",
                isError = descriptionInvalid,
            )
            RtTextField(
                label = "Peso",
                value = weight,
                onValueChange = onWeightChange,
                modifier = Modifier.weight(WeightWeight),
                placeholder = "0",
                isError = weightInvalid,
                suffix = "kg",
                keyboardType = KeyboardType.Decimal,
            )
            Box(
                modifier = Modifier.height(RtSize.inputHeight),
                contentAlignment = Alignment.Center,
            ) {
                RtIconButton(
                    icon = Icons.Rounded.Delete,
                    contentDescription = "Quitar ítem",
                    onClick = onRemove,
                )
            }
        }
        if (error != null) {
            Row(
                modifier = Modifier.padding(top = RtSpacing.s1),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconCaption),
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

private const val DescriptionWeight = 1.7f
private const val WeightWeight = 1f

@Preview(name = "Con datos, vacío y con error", showBackground = true)
@Composable
private fun RtItemRowPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtItemRow(
                    description = "Botellas de plástico",
                    onDescriptionChange = {},
                    weight = "2.5",
                    onWeightChange = {},
                    onRemove = {},
                )
                RtItemRow(
                    description = "",
                    onDescriptionChange = {},
                    weight = "",
                    onWeightChange = {},
                    onRemove = {},
                )
                RtItemRow(
                    description = "",
                    onDescriptionChange = {},
                    weight = "600",
                    onWeightChange = {},
                    onRemove = {},
                    error = "Escribe el material y un peso mayor a 0 y hasta 500 kg",
                    descriptionInvalid = true,
                    weightInvalid = true,
                )
            }
        }
    }
}
