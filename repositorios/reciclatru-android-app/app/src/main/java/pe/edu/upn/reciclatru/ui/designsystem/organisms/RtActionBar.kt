package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtElevation
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Barra fija al pie con la acción principal de la pantalla y, como mucho, una
 * secundaria (HU-05, HU-07, HU-08, HU-11, HU-12, HU-16). Solo aparecen las acciones
 * posibles en el estado actual y se queda sobre el teclado mientras se escribe.
 *
 * @param primary la acción principal, a la derecha. Pásala con `fullWidth = true` para
 *   que ocupe su mitad de la barra.
 * @param secondary la acción secundaria, a la izquierda; también con `fullWidth = true`.
 */
@Composable
fun RtActionBar(
    primary: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondary: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = RtElevation.level2)
            .background(colorScheme.surface)
            .imePadding(),
    ) {
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outline)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = RtSpacing.s4, vertical = RtSpacing.s3),
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
        ) {
            if (secondary != null) {
                Box(modifier = Modifier.weight(1f)) { secondary() }
            }
            Box(modifier = Modifier.weight(1f)) { primary() }
        }
    }
}

@Preview(name = "Una y dos acciones", showBackground = true, heightDp = 240)
@Composable
private fun RtActionBarPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s4)) {
                RtActionBar(
                    primary = {
                        RtButton(text = "Publicar pedido", onClick = {}, fullWidth = true)
                    },
                )
                RtActionBar(
                    primary = {
                        RtButton(text = "Recolectado", onClick = {}, fullWidth = true)
                    },
                    secondary = {
                        RtButton(
                            text = "Cancelar pedido",
                            onClick = {},
                            variant = RtButtonVariant.Secondary,
                            fullWidth = true,
                        )
                    },
                )
                RtActionBar(
                    primary = {
                        RtButton(text = "Guardando.", onClick = {}, loading = true, fullWidth = true)
                    },
                    secondary = {
                        RtButton(text = "Volver", onClick = {}, variant = RtButtonVariant.Text, fullWidth = true)
                    },
                )
            }
        }
    }
}
