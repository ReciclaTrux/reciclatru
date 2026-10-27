package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/** Tonos de [RtInlineAlert]: cada uno tiene su fondo, su texto y su ícono. */
enum class RtInlineAlertTone { Error, Warning, Info, Success }

/**
 * Aviso dentro del contenido, con ícono y texto (HU-02, HU-08, HU-10, HU-15).
 * Para avisos que se quedan en pantalla; un aviso pasajero es [RtSnackbar].
 *
 * @param tone error para "Correo o contraseña incorrectos.", warning para precaución,
 *   info para información con acción y success para recordatorios de lo ya hecho.
 * @param title línea en negrita opcional encima del mensaje.
 * @param actionLabel texto de la acción opcional, por ejemplo "Activar ubicación".
 */
@Composable
fun RtInlineAlert(
    tone: RtInlineAlertTone,
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val containerColor = when (tone) {
        RtInlineAlertTone.Error -> colorScheme.errorContainer
        RtInlineAlertTone.Warning -> rtColors.warningContainer
        RtInlineAlertTone.Info -> rtColors.infoContainer
        RtInlineAlertTone.Success -> colorScheme.secondaryContainer
    }
    val contentColor = when (tone) {
        RtInlineAlertTone.Error -> colorScheme.onErrorContainer
        RtInlineAlertTone.Warning -> rtColors.onWarningContainer
        RtInlineAlertTone.Info -> rtColors.onInfoContainer
        RtInlineAlertTone.Success -> colorScheme.onSecondaryContainer
    }
    val icon = iconOf(tone)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RtRadius.md))
            .background(containerColor)
            .padding(horizontal = RtSpacing.s4, vertical = RtSpacing.s3),
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(RtSize.iconMd),
            tint = contentColor,
        )
        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(
                    text = title,
                    style = RtTextStyles.bodyStrong,
                    color = contentColor,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = contentColor,
            )
            if (actionLabel != null && onAction != null) {
                RtButton(
                    text = actionLabel,
                    onClick = onAction,
                    modifier = Modifier.padding(top = RtSpacing.s2),
                    variant = RtButtonVariant.Text,
                    size = RtButtonSize.Compact,
                )
            }
        }
    }
}

private fun iconOf(tone: RtInlineAlertTone): ImageVector = when (tone) {
    RtInlineAlertTone.Error -> Icons.Rounded.Error
    RtInlineAlertTone.Warning -> Icons.Rounded.Warning
    RtInlineAlertTone.Info -> Icons.Rounded.Info
    RtInlineAlertTone.Success -> Icons.Rounded.Recycling
}

@Preview(name = "Cuatro tonos", showBackground = true, heightDp = 460)
@Composable
private fun RtInlineAlertPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtInlineAlert(
                    tone = RtInlineAlertTone.Error,
                    message = "Correo o contraseña incorrectos.",
                )
                RtInlineAlert(
                    tone = RtInlineAlertTone.Warning,
                    title = "Juan Pérez podría estar en camino",
                    message = "Avísale antes de cancelar: 987 654 321",
                )
                RtInlineAlert(
                    tone = RtInlineAlertTone.Info,
                    message = "Activa tu ubicación para ver la distancia",
                    actionLabel = "Activar ubicación",
                    onAction = {},
                )
                RtInlineAlert(
                    tone = RtInlineAlertTone.Success,
                    message = "El vecino marcará el pedido como Recolectado cuando le entregues el material.",
                )
            }
        }
    }
}

@Preview(name = "Texto largo", showBackground = true)
@Composable
private fun RtInlineAlertLongPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(RtSpacing.s4)) {
                RtInlineAlert(
                    tone = RtInlineAlertTone.Warning,
                    title = "El pedido es grande",
                    message = "Lleva los tres sacos en dos viajes para no cargar de más en el camino.",
                )
            }
        }
    }
}
