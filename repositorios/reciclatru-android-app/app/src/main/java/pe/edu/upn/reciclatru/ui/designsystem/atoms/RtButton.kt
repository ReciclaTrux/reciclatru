package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtFocusRing

/** Variantes de [RtButton]. Una sola primaria por pantalla. */
enum class RtButtonVariant { Primary, Secondary, Text, Danger }

/** Tamaños de [RtButton]: normal (52 px) y compacto para dentro de tarjetas (40 px). */
enum class RtButtonSize { Normal, Compact }

/**
 * Botón de acción con texto.
 *
 * @param variant [RtButtonVariant.Primary] para la acción principal,
 *   [RtButtonVariant.Secondary] para una alternativa o recuperación,
 *   [RtButtonVariant.Text] para acciones menores y [RtButtonVariant.Danger] solo para
 *   confirmar algo destructivo dentro de un diálogo.
 * @param icon ícono opcional al inicio.
 * @param loading muestra [RtSpinner] y bloquea los toques; el texto debe ir en gerundio.
 * @param fullWidth ocupa todo el ancho disponible.
 */
@Composable
fun RtButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RtButtonVariant = RtButtonVariant.Primary,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    fullWidth: Boolean = false,
    size: RtButtonSize = RtButtonSize.Normal,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = RoundedCornerShape(RtRadius.md)
    val isCompact = size == RtButtonSize.Compact

    val containerColor = when {
        !enabled -> rtColors.disabledBg
        variant == RtButtonVariant.Primary -> if (pressed) rtColors.primaryPressed else colorScheme.primary
        variant == RtButtonVariant.Secondary -> colorScheme.surface
        variant == RtButtonVariant.Danger -> colorScheme.error
        else -> Color.Transparent
    }
    val contentColor = when {
        !enabled -> rtColors.disabledInk
        variant == RtButtonVariant.Primary -> colorScheme.onPrimary
        variant == RtButtonVariant.Secondary || variant == RtButtonVariant.Text -> colorScheme.primary
        else -> colorScheme.onError
    }
    val border = if (enabled && variant == RtButtonVariant.Secondary) {
        BorderStroke(RtStroke.control, colorScheme.primary)
    } else {
        null
    }
    val horizontalPadding = when {
        isCompact -> RtSpacing.s4
        variant == RtButtonVariant.Text -> RtSpacing.s3
        else -> RtSpacing.s5
    }

    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier
            .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
            .height(if (isCompact) RtSize.buttonHeightSmall else RtSize.buttonHeight)
            .rtFocusRing(visible = focused, shape = shape, color = rtColors.primaryPressed),
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = rtColors.disabledBg,
            disabledContentColor = rtColors.disabledInk,
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        elevation = null,
        interactionSource = interactionSource,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
        ) {
            if (loading) {
                RtSpinner(color = LocalContentColor.current)
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                )
            }
            Text(
                text = text,
                style = if (isCompact) RtTextStyles.buttonSmall else RtTextStyles.button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Variantes", showBackground = true)
@Composable
private fun RtButtonVariantsPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
                    RtButton(text = "Publicar pedido", onClick = {})
                    RtButton(text = "Reintentar", onClick = {}, variant = RtButtonVariant.Secondary)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
                    RtButton(text = "Agregar ítem", onClick = {}, variant = RtButtonVariant.Text)
                    RtButton(text = "Sí, cancelar pedido", onClick = {}, variant = RtButtonVariant.Danger)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
                    RtButton(text = "Llamar", onClick = {}, icon = Icons.Rounded.Call)
                    RtButton(text = "Elegir", onClick = {}, size = RtButtonSize.Compact)
                }
            }
        }
    }
}

@Preview(name = "Estados", showBackground = true)
@Composable
private fun RtButtonStatesPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtButton(text = "Publicando…", onClick = {}, loading = true)
                RtButton(
                    text = "Ya aceptaste este pedido",
                    onClick = {},
                    icon = Icons.Rounded.Check,
                    enabled = false,
                )
                RtButton(text = "Deshabilitado secundario", onClick = {}, variant = RtButtonVariant.Secondary, enabled = false)
            }
        }
    }
}

@Preview(name = "Ancho completo", showBackground = true)
@Composable
private fun RtButtonFullWidthPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            ) {
                RtButton(text = "Crear cuenta", onClick = {}, fullWidth = true)
                RtButton(text = "Guardar cambios", onClick = {}, variant = RtButtonVariant.Secondary, fullWidth = true)
            }
        }
    }
}
