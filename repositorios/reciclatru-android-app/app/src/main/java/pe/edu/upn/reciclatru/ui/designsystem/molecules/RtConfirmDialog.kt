package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtElevation
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Diálogo que pide confirmar una acción importante o que no se puede deshacer
 * (HU-07, HU-08, HU-12, HU-13, HU-16).
 *
 * @param confirmLabel repite el verbo del título ("Elegir", "Aceptar", "Sí, cancelar"),
 *   nunca "Aceptar" ni "OK" genéricos.
 * @param dismissLabel botón de salida, siempre de texto y a la izquierda.
 * @param destructive pinta el botón de confirmar en `error` para cancelar o descartar.
 * @param loading el botón de confirmar muestra carga y el diálogo no se cierra tocando
 *   fuera ni con el botón de volver.
 * @param notice aviso opcional entre el mensaje y los botones, por ejemplo
 *   [RtInlineAlert] de precaución con el teléfono del reciclador.
 */
@Composable
fun RtConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String = "Volver",
    destructive: Boolean = false,
    loading: Boolean = false,
    notice: (@Composable () -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !loading,
            dismissOnClickOutside = !loading,
        ),
    ) {
        RtConfirmDialogContent(
            title = title,
            message = message,
            confirmLabel = confirmLabel,
            onConfirm = onConfirm,
            onDismiss = onDismiss,
            modifier = modifier,
            dismissLabel = dismissLabel,
            destructive = destructive,
            loading = loading,
            notice = notice,
        )
    }
}

@Composable
private fun RtConfirmDialogContent(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String,
    destructive: Boolean,
    loading: Boolean,
    notice: (@Composable () -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = RtSize.dialogMaxWidth)
            .shadow(elevation = RtElevation.level3, shape = shape, clip = false),
        shape = shape,
        color = colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(RtSpacing.s5)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
            )
            Text(
                text = message,
                modifier = Modifier.padding(top = RtSpacing.s2, bottom = RtSpacing.s4),
                style = MaterialTheme.typography.bodyLarge,
                color = colorScheme.onSurfaceVariant,
            )
            if (notice != null) {
                Box(modifier = Modifier.padding(bottom = RtSpacing.s4)) {
                    notice()
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RtButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    variant = RtButtonVariant.Text,
                    enabled = !loading,
                )
                RtButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    variant = if (destructive) RtButtonVariant.Danger else RtButtonVariant.Primary,
                    loading = loading,
                )
            }
        }
    }
}

@Preview(name = "Elegir", showBackground = true, heightDp = 420)
@Composable
private fun RtConfirmDialogPreview() {
    RtTheme {
        DialogScrimPreview {
            RtConfirmDialogContent(
                title = "¿Elegir a Juan Pérez?",
                message = "Él recogerá tu pedido. Los demás recicladores quedarán descartados.",
                confirmLabel = "Elegir",
                onConfirm = {},
                onDismiss = {},
                dismissLabel = "Volver",
                destructive = false,
                loading = false,
                notice = null,
            )
        }
    }
}

@Preview(name = "Cancelar con aviso", showBackground = true, heightDp = 520)
@Composable
private fun RtConfirmDialogDestructivePreview() {
    RtTheme {
        DialogScrimPreview {
            RtConfirmDialogContent(
                title = "¿Seguro que deseas cancelar?",
                message = "El pedido pasará a tu historial y no se podrá reactivar.",
                confirmLabel = "Sí, cancelar",
                onConfirm = {},
                onDismiss = {},
                dismissLabel = "Volver",
                destructive = true,
                loading = false,
                notice = {
                    RtInlineAlert(
                        tone = RtInlineAlertTone.Warning,
                        title = "Juan Pérez podría estar en camino",
                        message = "Avísale antes de cancelar: 987 654 321",
                        actionLabel = "Llamar a Juan",
                        onAction = {},
                    )
                },
            )
        }
    }
}

@Preview(name = "Confirmando", showBackground = true, heightDp = 420)
@Composable
private fun RtConfirmDialogLoadingPreview() {
    RtTheme {
        DialogScrimPreview {
            RtConfirmDialogContent(
                title = "¿Aceptar este pedido?",
                message = "Al aceptar, el vecino verá tu nombre y celular",
                confirmLabel = "Aceptando.",
                onConfirm = {},
                onDismiss = {},
                dismissLabel = "Volver",
                destructive = false,
                loading = true,
                notice = null,
            )
        }
    }
}

@Composable
private fun DialogScrimPreview(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.scrim)
            .padding(RtSpacing.s6),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
