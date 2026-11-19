package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAvatar
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Tarjeta de un reciclador que aceptó el pedido (HU-13): una fila con avatar, nombre,
 * "celular · cuándo aceptó", botón de llamada y botón "Elegir".
 *
 * @param name nombre completo del reciclador.
 * @param phone celular de nueve dígitos; se muestra agrupado en tres y es el que marca
 *   el botón de llamada.
 * @param acceptedAt cuándo aceptó, ya formateado, por ejemplo "hoy, 9:40".
 * @param onCall abre el marcador con el celular.
 * @param onChoose pide elegir a este reciclador; `null` oculta el botón (el pedido ya no
 *   permite elegir).
 * @param choosing muestra "Elegir" cargando mientras se envía la elección.
 * @param chooseEnabled deshabilita "Elegir" sin quitarlo.
 */
@Composable
fun RtAcceptanceCard(
    name: String,
    phone: String,
    acceptedAt: String,
    modifier: Modifier = Modifier,
    onCall: () -> Unit = {},
    onChoose: (() -> Unit)? = null,
    choosing: Boolean = false,
    chooseEnabled: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s3),
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
        verticalAlignment = Alignment.CenterVertically,
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
                text = "${groupedPhone(phone)} · $acceptedAt",
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
            )
        }
        RtIconButton(
            icon = Icons.Rounded.Call,
            contentDescription = "Llamar a $name",
            onClick = onCall,
            variant = RtIconButtonVariant.Tonal,
        )
        if (onChoose != null) {
            RtButton(
                text = "Elegir",
                onClick = onChoose,
                loading = choosing,
                enabled = chooseEnabled,
                size = RtButtonSize.Compact,
            )
        }
    }
}

/** Agrupa un celular de nueve dígitos en tres: "987 654 321". */
private fun groupedPhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    if (digits.length != 9) return phone
    return "${digits.substring(0, 3)} ${digits.substring(3, 6)} ${digits.substring(6)}"
}

@Preview(name = "Con elegir, cargando y sin elegir", showBackground = true)
@Composable
private fun RtAcceptanceCardPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtAcceptanceCard(
                    name = "Juan Pérez",
                    phone = "987654321",
                    acceptedAt = "hoy, 9:40",
                    onChoose = {},
                )
                RtAcceptanceCard(
                    name = "María Ríos",
                    phone = "956111204",
                    acceptedAt = "hoy, 10:42",
                    onChoose = {},
                    choosing = true,
                )
                RtAcceptanceCard(
                    name = "Carlos Vega",
                    phone = "912330518",
                    acceptedAt = "ayer, 11:05",
                )
            }
        }
    }
}
