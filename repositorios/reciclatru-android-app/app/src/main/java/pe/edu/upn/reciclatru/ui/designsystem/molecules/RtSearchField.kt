package pe.edu.upn.reciclatru.ui.designsystem.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtElevation
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/**
 * Buscador en forma de píldora que flota sobre el mapa para ir a una dirección (HU-04).
 * Solo se muestra si el teléfono tiene el servicio de búsqueda de direcciones.
 *
 * Las sugerencias las entrega la feature: el componente solo muestra hasta cuatro, avisa
 * cuál se eligió y muestra el error del geocoder. La feature hace el debounce y llama al
 * geocoder; el componente no lo conoce.
 *
 * @param query texto escrito por la persona.
 * @param onSearch se dispara al confirmar desde el teclado (acción Buscar).
 * @param error texto que aparece bajo el buscador, por ejemplo "No encontramos esa
 *   dirección. Mueve el mapa a mano.".
 * @param suggestions direcciones sugeridas por la feature; se muestran hasta cuatro.
 * @param suggestionsLoading muestra el spinner mientras la feature consulta al geocoder.
 * @param onSuggestionSelected avisa cuál se eligió. Quien lo usa debe actualizar [query]
 *   con ese texto; el componente no dispara [onQueryChange] al elegir.
 */
@Composable
fun RtSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    suggestions: List<String> = emptyList(),
    suggestionsLoading: Boolean = false,
    onSuggestionSelected: ((String) -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val shape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusManager = LocalFocusManager.current
    val inspectionMode = LocalInspectionMode.current
    val density = LocalDensity.current
    val windowHeight = with(density) {
        LocalWindowInfo.current.containerSize.height.toDp()
    }
    val imeBottom = with(density) {
        WindowInsets.ime.getBottom(this).toDp()
    }

    // Al escribir se ocultan las sugerencias de la consulta anterior; vuelven a mostrarse
    // cuando la feature entrega una lista nueva.
    var suppressSuggestions by remember { mutableStateOf(false) }
    var fieldBottom by remember { mutableStateOf(0.dp) }
    LaunchedEffect(suggestions) {
        if (suggestions.isNotEmpty()) suppressSuggestions = false
    }

    val borderColor = when {
        error != null -> colorScheme.error
        focused -> colorScheme.primary
        else -> colorScheme.outline
    }
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface)
    val visibleSuggestions = suggestions.take(MaxSuggestions)
    // No tapa el mapa ni el teclado: si no queda al menos una fila libre debajo del
    // buscador, la lista no se dibuja (el usuario sigue escribiendo o cierra el teclado).
    val availableHeight = windowHeight - imeBottom - fieldBottom - RtSpacing.s2
    val showSuggestions = (focused || inspectionMode) &&
        query.isNotBlank() &&
        visibleSuggestions.isNotEmpty() &&
        !suppressSuggestions &&
        (inspectionMode || availableHeight >= RtSize.touchTarget)

    val onSuggestionChosen: (String) -> Unit = { suggestion ->
        suppressSuggestions = true
        focusManager.clearFocus()
        onSuggestionSelected?.invoke(suggestion)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    fieldBottom = with(density) { coordinates.boundsInWindow().bottom.toDp() }
                }
                .height(RtSize.inputHeight)
                .shadow(elevation = RtElevation.level2, shape = shape, clip = false)
                .clip(shape)
                .background(colorScheme.surface)
                .border(RtStroke.control, borderColor, shape)
                .padding(horizontal = RtSpacing.s4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(RtSize.iconMd),
                tint = colorScheme.onSurfaceVariant,
            )
            BasicTextField(
                value = query,
                onValueChange = { value ->
                    suppressSuggestions = true
                    onQueryChange(value)
                },
                modifier = Modifier.weight(1f),
                textStyle = textStyle,
                cursorBrush = SolidColor(colorScheme.primary),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Busca una calle o lugar",
                                style = textStyle,
                                color = rtColors.inkSubtle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (suggestionsLoading) {
                RtSpinner()
            }
        }
        if (showSuggestions) {
            // La lista se acota al alto que queda debajo del buscador y dentro de lo que
            // el teclado deja libre.
            SuggestionsList(
                suggestions = visibleSuggestions,
                maxHeight = if (inspectionMode) {
                    RtSize.touchTarget * MaxSuggestions
                } else {
                    availableHeight.coerceAtMost(RtSize.touchTarget * MaxSuggestions)
                },
                onSelect = onSuggestionChosen,
                modifier = Modifier.padding(top = RtSpacing.s1),
            )
        }
        if (error != null) {
            Row(
                modifier = Modifier.padding(top = RtSpacing.s1),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Error,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconCaption),
                    tint = colorScheme.error,
                )
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun SuggestionsList(
    suggestions: List<String>,
    maxHeight: Dp,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(RtRadius.md)
    val textStyle = MaterialTheme.typography.bodyLarge

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .shadow(elevation = RtElevation.level2, shape = shape, clip = false)
            .clip(shape)
            .background(colorScheme.surface)
            .border(RtStroke.divider, colorScheme.outlineVariant, shape),
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            suggestions.forEachIndexed { index, suggestion ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = RtStroke.divider,
                        color = colorScheme.outlineVariant,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RtSize.touchTarget)
                        .clickable { onSelect(suggestion) }
                        .padding(horizontal = RtSpacing.s4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(RtSize.iconMd),
                        tint = colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = suggestion,
                        style = textStyle,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** El sistema define hasta cuatro sugerencias visibles. */
private const val MaxSuggestions = 4

@Preview(name = "Vacío, con texto y error", showBackground = true, heightDp = 260)
@Composable
private fun RtSearchFieldPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                RtSearchField(query = "", onQueryChange = {}, onSearch = {})
                RtSearchField(query = "Av. Larco 1250", onQueryChange = {}, onSearch = {})
                RtSearchField(
                    query = "Calle que no existe",
                    onQueryChange = {},
                    onSearch = {},
                    error = "No encontramos esa dirección. Mueve el mapa a mano.",
                )
            }
        }
    }
}

@Preview(name = "Sugerencias y cargando", showBackground = true, heightDp = 360)
@Composable
private fun RtSearchFieldSuggestionsPreview() {
    val suggestions = listOf(
        "Av. Larco 1250, Víctor Larco Herrera",
        "Av. Larco 1200, Víctor Larco Herrera",
        "Av. Larco 1302, Víctor Larco Herrera",
    )
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
            ) {
                RtSearchField(
                    query = "Av. Larco",
                    onQueryChange = {},
                    onSearch = {},
                    suggestions = suggestions,
                    onSuggestionSelected = {},
                )
                RtSearchField(
                    query = "Av. Larco",
                    onQueryChange = {},
                    onSearch = {},
                    suggestionsLoading = true,
                )
            }
        }
    }
}
