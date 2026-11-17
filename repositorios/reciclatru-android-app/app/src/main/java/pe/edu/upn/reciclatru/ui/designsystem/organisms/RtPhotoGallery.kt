package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumb
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumbSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Fotos de un pedido en su detalle, con el título "Fotos (N)".
 *
 * @param photos URL de cada foto; hasta tres.
 */
@Composable
fun RtPhotoGallery(
    photos: List<Any>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Text(
            text = "Fotos (${photos.size})",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            photos.forEachIndexed { index, photo ->
                RtPhotoThumb(
                    model = photo,
                    size = RtPhotoThumbSize.Large,
                    contentDescription = "Foto ${index + 1} del pedido",
                )
            }
        }
    }
}

@Preview(name = "Dos fotos", showBackground = true)
@Composable
private fun RtPhotoGalleryPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtPhotoGallery(photos = listOf("a", "b"), modifier = Modifier.padding(RtSpacing.s4))
        }
    }
}
