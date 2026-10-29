package pe.edu.upn.reciclatru.ui.designsystem.organisms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Acción única de la derecha de [RtTopAppBar]; si hay más acciones, van en el contenido.
 *
 * @param contentDescription descripción accesible, por ejemplo "Editar pedido".
 */
data class RtAppBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Barra superior con título y, en pantallas internas, botón de volver y hasta una acción.
 * En las pantallas raíz (Mis pedidos, Disponibles, Aceptados, Perfil) no lleva flecha.
 *
 * @param onBack si existe, muestra la flecha de volver.
 */
@Composable
fun RtTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    action: RtAppBarAction? = null,
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth().background(colorScheme.surface)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(RtSize.appBarHeight)
                .padding(horizontal = RtSpacing.s1),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                RtIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    onClick = onBack,
                    variant = RtIconButtonVariant.Ink,
                )
            }
            Text(
                text = title,
                modifier = Modifier
                    .weight(1f)
                    .then(if (onBack == null) Modifier.padding(start = RtSpacing.s3) else Modifier),
                style = MaterialTheme.typography.titleLarge,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (action != null) {
                RtIconButton(
                    icon = action.icon,
                    contentDescription = action.contentDescription,
                    onClick = action.onClick,
                    variant = RtIconButtonVariant.Ink,
                )
            }
        }
        HorizontalDivider(thickness = RtStroke.divider, color = colorScheme.outline)
    }
}

@Preview(name = "Raíz y pantalla interna", showBackground = true, heightDp = 200)
@Composable
private fun RtTopAppBarPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                RtTopAppBar(title = "Mis pedidos")
                RtTopAppBar(
                    title = "Detalle del pedido",
                    onBack = {},
                    action = RtAppBarAction(
                        icon = Icons.Rounded.Edit,
                        contentDescription = "Editar pedido",
                        onClick = {},
                    ),
                )
            }
        }
    }
}

@Preview(name = "Título largo", showBackground = true)
@Composable
private fun RtTopAppBarLongTitlePreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RtTopAppBar(
                title = "Pedido de botellas y cartón en Víctor Larco",
                onBack = {},
            )
        }
    }
}
