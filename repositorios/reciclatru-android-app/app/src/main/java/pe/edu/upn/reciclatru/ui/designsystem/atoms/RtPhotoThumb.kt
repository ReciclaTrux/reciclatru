package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import coil3.compose.AsyncImage
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/** Tamaños de [RtPhotoThumb]: listas, formulario y detalle. */
enum class RtPhotoThumbSize { Small, Medium, Large }

/**
 * Miniatura cuadrada de una foto del pedido. Sin foto, o mientras carga, muestra un
 * ícono sobre `surface-variant`.
 *
 * @param model la foto: una URL de la API o la dirección local de la foto elegida;
 *   `null` deja solo el ícono.
 * @param onRemove muestra el botón para quitar la foto; requiere una descripción en
 *   [contentDescription].
 */
@Composable
fun RtPhotoThumb(
    model: Any?,
    modifier: Modifier = Modifier,
    size: RtPhotoThumbSize = RtPhotoThumbSize.Small,
    contentDescription: String? = null,
    onRemove: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val side = when (size) {
        RtPhotoThumbSize.Small -> RtSize.thumbSm
        RtPhotoThumbSize.Medium -> RtSize.thumbMd
        RtPhotoThumbSize.Large -> RtSize.thumbLg
    }

    Box(modifier = modifier.size(side)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(RtRadius.sm))
                .background(colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Inventory2,
                contentDescription = null,
                modifier = Modifier.size(RtSize.iconMd),
                tint = colorScheme.onSurfaceVariant,
            )
            if (model != null) {
                AsyncImage(
                    model = model,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        if (onRemove != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(RtSpacing.s1)
                    .size(RtSize.iconMd)
                    .clip(CircleShape)
                    .background(colorScheme.inverseSurface)
                    .clickable(
                        onClickLabel = "Quitar foto",
                        role = Role.Button,
                        onClick = onRemove,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Quitar foto",
                    modifier = Modifier.size(RtSize.iconCaption),
                    tint = colorScheme.inverseOnSurface,
                )
            }
        }
    }
}

@Preview(name = "Tamaños y quitar", showBackground = true)
@Composable
private fun RtPhotoThumbPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(RtSpacing.s4),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                verticalAlignment = Alignment.Bottom,
            ) {
                RtPhotoThumb(model = null)
                RtPhotoThumb(model = null, size = RtPhotoThumbSize.Medium, onRemove = {})
                RtPhotoThumb(model = null, size = RtPhotoThumbSize.Large)
            }
        }
    }
}
