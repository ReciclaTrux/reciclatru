package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumb
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtPhotoThumbSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

private const val MaxZoom = 4f
private const val DoubleTapZoom = 2.5f

/**
 * Fotos de un pedido en su detalle, con el título "Fotos (N)", en una fila horizontal. Al
 * tocar una foto se abre en pantalla completa, donde se puede hacer zoom y deslizar entre
 * las fotos.
 *
 * @param photos URL de cada foto; hasta tres.
 */
@Composable
fun RtPhotoGallery(
    photos: List<Any>,
    modifier: Modifier = Modifier,
) {
    var openedIndex by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s2),
    ) {
        Text(
            text = "Fotos (${photos.size})",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
        ) {
            photos.forEachIndexed { index, photo ->
                RtPhotoThumb(
                    model = photo,
                    size = RtPhotoThumbSize.Large,
                    contentDescription = "Foto ${index + 1} del pedido",
                    modifier = Modifier
                        .clickable(onClickLabel = "Ver foto en pantalla completa", role = Role.Image) {
                            openedIndex = index
                        }
                        .testTag("foto_pedido_$index"),
                )
            }
        }
    }

    openedIndex?.let { index ->
        PhotoViewer(
            photos = photos,
            initialIndex = index.coerceIn(0, (photos.size - 1).coerceAtLeast(0)),
            onDismiss = { openedIndex = null },
        )
    }
}

@Composable
private fun PhotoViewer(
    photos: List<Any>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(initialPage = initialIndex) { photos.size }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.inverseSurface)
                .testTag("visor_fotos"),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                ZoomablePhoto(
                    model = photos[page],
                    contentDescription = "Foto ${page + 1} de ${photos.size}",
                )
            }
            Row(
                modifier = Modifier
                    .safeDrawingPadding()
                    .fillMaxWidth()
                    .padding(horizontal = RtSpacing.s4, vertical = RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${pagerState.currentPage + 1} / ${photos.size}",
                    style = MaterialTheme.typography.titleMedium,
                    color = colorScheme.inverseOnSurface,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .size(RtSize.touchTarget)
                        .clip(CircleShape)
                        .clickable(onClickLabel = "Cerrar", role = Role.Button, onClick = onDismiss)
                        .testTag("btn_cerrar_visor"),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Cerrar",
                        modifier = Modifier.size(RtSize.iconMd),
                        tint = colorScheme.inverseOnSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun ZoomablePhoto(model: Any, contentDescription: String) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, MaxZoom)
        offset = if (scale == 1f) Offset.Zero else offset + panChange
    }

    SubcomposeAsyncImage(
        model = model,
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        if (scale > 1f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            scale = DoubleTapZoom
                        }
                    },
                )
            }
            .transformable(state = transformState, canPan = { scale > 1f })
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
        error = {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.BrokenImage,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.stateIconGlyph),
                    tint = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        },
    )
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
