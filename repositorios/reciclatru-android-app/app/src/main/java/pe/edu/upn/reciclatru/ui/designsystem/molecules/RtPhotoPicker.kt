package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumb
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumbSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/**
 * Fotos opcionales del pedido (HU-09): miniaturas con botón para quitar y el recuadro
 * Agregar foto. Al llegar al máximo, el recuadro se desactiva y dice "Máximo N".
 *
 * @param photos fotos elegidas: URL de la API o dirección local.
 * @param onAdd abre la cámara o la galería; lo decide la pantalla.
 * @param onRemove recibe la posición de la foto a quitar.
 * @param enabled en `false` no deja agregar ni quitar, por ejemplo mientras se publica.
 */
@Composable
fun RtPhotoPicker(
    photos: List<Any>,
    onAdd: () -> Unit,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
    maxPhotos: Int = 3,
    enabled: Boolean = true,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val isFull = photos.size >= maxPhotos
    val canAdd = enabled && !isFull
    val tileShape = RoundedCornerShape(RtRadius.sm)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Text(
            text = "Fotos (opcional)",
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            photos.forEachIndexed { index, photo ->
                RtPhotoThumb(
                    model = photo,
                    size = RtPhotoThumbSize.Medium,
                    contentDescription = "Foto ${index + 1} del pedido",
                    onRemove = if (enabled) {
                        { onRemove(index) }
                    } else {
                        null
                    },
                )
            }
            Column(
                modifier = Modifier
                    .size(RtSize.thumbMd)
                    .clip(tileShape)
                    .background(if (canAdd) colorScheme.surface else rtColors.disabledBg)
                    .border(RtStroke.control, if (canAdd) colorScheme.primary else colorScheme.outlineVariant, tileShape)
                    .clickable(enabled = canAdd, role = Role.Button, onClick = onAdd)
                    .padding(RtSpacing.s1),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AddAPhoto,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = if (canAdd) colorScheme.primary else rtColors.disabledInk,
                )
                Text(
                    text = if (isFull) "Máximo $maxPhotos" else "Agregar foto",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (canAdd) colorScheme.primary else rtColors.disabledInk,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Toma una foto o elígela de tu galería",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${photos.size}/$maxPhotos",
                style = MaterialTheme.typography.labelMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Vacío, con fotos y lleno", showBackground = true)
@Composable
private fun RtPhotoPickerPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
            ) {
                RtPhotoPicker(photos = emptyList(), onAdd = {}, onRemove = {})
                RtPhotoPicker(photos = listOf("a", "b"), onAdd = {}, onRemove = {})
                RtPhotoPicker(photos = listOf("a", "b", "c"), onAdd = {}, onRemove = {})
            }
        }
    }
}
