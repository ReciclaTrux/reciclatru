package pe.edu.upn.reciclatru.ui.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Elevación del sistema (`elevation-1` a `elevation-3` de `tokens.json`).
 * Compose dibuja una sola sombra por control, así que cada nivel usa el desenfoque de la
 * capa más fuerte del sistema. Solo la usan las tarjetas de lista (`level1`), la barra de
 * acción fija, el buscador y el snackbar (`level2`), y los diálogos y la hoja del mapa
 * (`level3`). Ningún otro elemento tiene sombra.
 */
object RtElevation {
    val level1 = 3.dp
    val level2 = 6.dp
    val level3 = 24.dp
}
