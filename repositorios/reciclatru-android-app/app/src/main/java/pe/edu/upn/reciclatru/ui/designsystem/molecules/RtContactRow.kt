package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAvatar
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Avatar, nombre, un dato secundario y botón de llamada para una persona
 * (HU-12, HU-13, HU-15).
 *
 * @param subtitle celular o fecha de aceptación; si es un celular se agrupa en tres.
 * @param phone celular para llamar. Si es `null` no se muestra el botón: el backend
 *   decide, según el rol y el estado del pedido, si manda el teléfono.
 */
@Composable
fun RtContactRow(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    phone: String? = null,
    onCall: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        RtAvatar(name = name)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = RtTextStyles.bodyStrong,
                color = colorScheme.onSurface,
                maxLines = 2,
            )
            Text(
                text = groupedPhone(subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        if (phone != null) {
            RtIconButton(
                icon = Icons.Rounded.Call,
                contentDescription = "Llamar a $name",
                onClick = onCall,
                variant = RtIconButtonVariant.Tonal,
            )
        }
    }
}

/** Agrupa un celular de nueve dígitos en tres: "987 654 321". */
private fun groupedPhone(subtitle: String): String {
    val digits = subtitle.filter { it.isDigit() }
    val isPhone = digits.length == 9 && subtitle.none { it.isLetter() }
    if (!isPhone) return subtitle
    return "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
}

@Preview(name = "Con celular y con fecha", showBackground = true, heightDp = 200)
@Composable
private fun RtContactRowPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                RtContactRow(
                    name = "Juan Pérez",
                    subtitle = "987654321",
                    phone = "987654321",
                    onCall = {},
                )
                RtContactRow(
                    name = "María Ríos",
                    subtitle = "Aceptó hoy, 10:42",
                    phone = "948120337",
                    onCall = {},
                )
            }
        }
    }
}

@Preview(name = "Sin teléfono", showBackground = true)
@Composable
private fun RtContactRowWithoutPhonePreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(RtSpacing.s4)) {
                RtContactRow(
                    name = "María Ríos",
                    subtitle = "Esperando confirmación",
                )
            }
        }
    }
}
