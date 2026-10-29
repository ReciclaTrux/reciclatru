package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Círculo de 40 px con las iniciales de una persona. Es decorativo: el nombre
 * siempre se muestra escrito al lado.
 *
 * @param name nombre y apellido; se toman la primera letra del nombre y del apellido.
 */
@Composable
fun RtAvatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(RtSize.avatarMd)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initialsOf(name),
            style = RtTextStyles.avatarInitials,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
    }
}

private fun initialsOf(name: String): String {
    val words = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (words.isEmpty()) return ""
    val first = words.first().first().uppercaseChar()
    if (words.size == 1) return first.toString()
    val last = words.last().first().uppercaseChar()
    return "$first$last"
}

@Preview(name = "Iniciales", showBackground = true)
@Composable
private fun RtAvatarPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(RtSpacing.s4),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RtAvatar(name = "Juan Pérez")
                RtAvatar(name = "María Rodríguez")
                RtAvatar(name = "Ana")
            }
        }
    }
}
