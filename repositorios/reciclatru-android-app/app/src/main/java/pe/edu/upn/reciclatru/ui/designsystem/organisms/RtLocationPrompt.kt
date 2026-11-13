package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Explica para qué se usa la ubicación antes de mostrar el permiso del sistema (HU-10).
 *
 * @param onContinue la persona acepta: la pantalla muestra el permiso del sistema.
 * @param onDismiss la persona elige "Ahora no": la lista se ordena por fecha.
 */
@Composable
fun RtLocationPrompt(
    onContinue: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.lg)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape)
            .padding(RtSpacing.s5),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        Box(
            modifier = Modifier
                .size(RtSize.stateIcon)
                .clip(CircleShape)
                .background(colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.NearMe,
                contentDescription = null,
                modifier = Modifier.size(RtSize.stateIconGlyph),
                tint = colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = "Usamos tu ubicación solo para mostrar distancias",
            style = MaterialTheme.typography.titleMedium,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Así ves primero los pedidos más cercanos. No la guardamos ni la mostramos a los vecinos.",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        RtButton(
            text = "Continuar",
            onClick = onContinue,
            fullWidth = true,
            modifier = Modifier.padding(top = RtSpacing.s2),
        )
        RtButton(
            text = "Ahora no",
            onClick = onDismiss,
            variant = RtButtonVariant.Text,
        )
    }
}

@Preview(name = "Permiso de ubicación", showBackground = true)
@Composable
private fun RtLocationPromptPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtLocationPrompt(
                onContinue = {},
                onDismiss = {},
                modifier = Modifier.padding(RtSpacing.s4),
            )
        }
    }
}
