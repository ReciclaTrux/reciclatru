package pe.edu.upn.reciclatru.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAvatar
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadge
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadgeVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtIconButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtConfirmDialog
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtContactRow
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorKind
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtRole
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtRoleSelector
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSearchField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtAppBarAction
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNav
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtBottomNavItem
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtLatLng
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtMapPicker
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/**
 * Catálogo de lo implementado del sistema de diseño: tema y componentes con sus estados.
 * Solo existe en builds de debug.
 */
@Composable
fun RtCatalogScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentPadding = PaddingValues(vertical = RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
        ) {
            item { CatalogHeader() }
            item { ThemeSection() }
            item { AtomsSection() }
            item { MoleculesSection() }
            item { OrganismsSection() }
            // A lo ancho de toda la pantalla, como en una HU: la barra trae su propio
            // padding y así se revisa igual que en el prototipo.
            item { ActionBarCatalog() }
        }
    }
}

@Composable
private fun CatalogHeader() {
    Column(
        modifier = Modifier.padding(horizontal = RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
    ) {
        Text(
            text = "ReciclaTru",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.rtColors.ods12,
        )
        Text(
            text = "Catálogo del sistema de diseño",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = "Solo en builds de debug. Compara cada componente con su vista previa del sistema.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RtStroke.divider)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

@Composable
private fun ComponentTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun ThemeSection() {
    Column(
        modifier = Modifier.padding(horizontal = RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
    ) {
        SectionTitle(text = "Tema")
        ColorTokens()
        TypeTokens()
        ShapeAndSizeTokens()
    }
}

@Composable
private fun ColorTokens() {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val tokens = listOf(
        "ods-12" to rtColors.ods12,
        "primary" to colorScheme.primary,
        "primary-pressed" to rtColors.primaryPressed,
        "primary-container" to colorScheme.primaryContainer,
        "secondary" to colorScheme.secondary,
        "secondary-container" to colorScheme.secondaryContainer,
        "background" to colorScheme.background,
        "surface" to colorScheme.surface,
        "surface-variant" to colorScheme.surfaceVariant,
        "ink" to colorScheme.onSurface,
        "ink-muted" to colorScheme.onSurfaceVariant,
        "ink-subtle" to rtColors.inkSubtle,
        "outline" to colorScheme.outlineVariant,
        "outline-strong" to colorScheme.outline,
        "error" to colorScheme.error,
        "error-container" to colorScheme.errorContainer,
        "warning-container" to rtColors.warningContainer,
        "info-container" to rtColors.infoContainer,
        "status-pending" to rtColors.statusPendingBg,
        "status-confirmed" to rtColors.statusConfirmedBg,
        "status-collected" to rtColors.statusCollectedBg,
        "status-cancelled" to rtColors.statusCancelledBg,
        "status-chosen" to rtColors.statusChosenBg,
        "inverse-surface" to colorScheme.inverseSurface,
        "disabled-bg" to rtColors.disabledBg,
    )
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "Colores")
        tokens.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
                row.forEach { (name, color) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(RtSize.touchTarget)
                                .background(color, RoundedCornerShape(RtRadius.sm))
                                .border(
                                    RtStroke.control,
                                    colorScheme.outlineVariant,
                                    RoundedCornerShape(RtRadius.sm),
                                ),
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun TypeTokens() {
    val samples = listOf(
        "headline-lg 28/36 700" to MaterialTheme.typography.headlineMedium,
        "headline 24/32 700" to MaterialTheme.typography.headlineSmall,
        "title 20/28 700" to MaterialTheme.typography.titleLarge,
        "subtitle 18/24 600" to MaterialTheme.typography.titleMedium,
        "body 16/24 400" to MaterialTheme.typography.bodyLarge,
        "body-sm 14/20 400" to MaterialTheme.typography.bodyMedium,
        "label 14/20 600" to MaterialTheme.typography.labelLarge,
        "caption 13/18 600" to MaterialTheme.typography.labelMedium,
    )
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "Tipografía")
        samples.forEach { (name, style) ->
            Text(
                text = name,
                style = style,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
private fun ShapeAndSizeTokens() {
    val colorScheme = MaterialTheme.colorScheme
    val shapes = listOf(
        "radius-sm" to RoundedCornerShape(RtRadius.sm),
        "radius-md" to RoundedCornerShape(RtRadius.md),
        "radius-lg" to RoundedCornerShape(RtRadius.lg),
        "radius-full" to RoundedCornerShape(RtRadius.full),
    )
    val sizes = listOf(
        "touch-target" to RtSize.touchTarget,
        "button-height" to RtSize.buttonHeight,
        "button-height-sm" to RtSize.buttonHeightSmall,
        "input-height" to RtSize.inputHeight,
        "appbar-height" to RtSize.appBarHeight,
        "bottomnav-height" to RtSize.bottomNavHeight,
        "avatar-md" to RtSize.avatarMd,
        "thumb-sm" to RtSize.thumbSm,
        "thumb-md" to RtSize.thumbMd,
        "thumb-lg" to RtSize.thumbLg,
    )
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "Formas y tamaños")
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
            shapes.forEach { (name, shape) ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                ) {
                    Box(
                        modifier = Modifier
                            .size(RtSize.touchTarget)
                            .background(colorScheme.surfaceVariant, shape)
                            .border(RtStroke.control, colorScheme.outlineVariant, shape),
                    )
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
            }
        }
        sizes.forEach { (name, value) ->
            Text(
                text = "$name = ${value.value.toInt()} dp",
                style = MaterialTheme.typography.bodyMedium,
                color = colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "Espaciado: s1 4 · s2 8 · s3 12 · s4 16 · s5 24 · s6 32 · s7 48",
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AtomsSection() {
    Column(
        modifier = Modifier.padding(horizontal = RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
    ) {
        SectionTitle(text = "Átomos")
        ButtonCatalog()
        TextFieldCatalog()
        SpinnerCatalog()
        IconButtonCatalog()
        AvatarCatalog()
        BadgeCatalog()
    }
}

@Composable
private fun ButtonCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtButton")
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(text = "Publicar pedido", onClick = {})
            RtButton(text = "Reintentar", onClick = {}, variant = RtButtonVariant.Secondary)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(text = "Agregar ítem", onClick = {}, variant = RtButtonVariant.Text)
            RtButton(text = "Sí, cancelar pedido", onClick = {}, variant = RtButtonVariant.Danger)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(text = "Llamar", onClick = {}, icon = Icons.Rounded.Call)
            RtButton(text = "Elegir", onClick = {}, size = RtButtonSize.Compact)
        }
        RtButton(text = "Publicando…", onClick = {}, loading = true)
        RtButton(
            text = "Ya aceptaste este pedido",
            onClick = {},
            icon = Icons.Rounded.Check,
            enabled = false,
        )
        RtButton(text = "Crear cuenta", onClick = {}, fullWidth = true)
    }
}

@Composable
private fun TextFieldCatalog() {
    var email by remember { mutableStateOf("ana.torres@gmail.com") }
    var phone by remember { mutableStateOf("94812") }
    var weight by remember { mutableStateOf("2.5") }
    var reference by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("reciclo2026") }

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s4)) {
        ComponentTitle(text = "RtTextField")
        RtTextField(
            label = "Correo electrónico",
            value = email,
            onValueChange = { email = it },
            placeholder = "nombre@correo.com",
            leadingIcon = Icons.Rounded.Mail,
            keyboardType = KeyboardType.Email,
        )
        RtTextField(
            label = "Contraseña",
            value = password,
            onValueChange = { password = it },
            helper = "Mínimo 8 caracteres",
            leadingIcon = Icons.Rounded.Lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (passwordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
            trailingIconDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
            onTrailingIconClick = { passwordVisible = !passwordVisible },
        )
        RtTextField(
            label = "Celular",
            value = phone,
            onValueChange = { phone = it },
            error = "Debe tener 9 dígitos y empezar con 9",
            leadingIcon = Icons.Rounded.PhoneIphone,
            keyboardType = KeyboardType.Phone,
            maxLength = 9,
        )
        RtTextField(
            label = "Peso",
            value = weight,
            onValueChange = { weight = it },
            suffix = "kg",
            keyboardType = KeyboardType.Decimal,
        )
        RtTextField(
            label = "Referencia (opcional)",
            value = reference,
            onValueChange = { reference = it },
            placeholder = "Dpto. 302, puerta negra, frente al parque",
            helper = "Ayuda al reciclador a encontrar tu puerta",
            maxLength = 150,
            multiline = true,
        )
        RtTextField(
            label = "Rol",
            value = "Vecino",
            onValueChange = {},
            readOnly = true,
        )
    }
}

@Composable
private fun SpinnerCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "RtSpinner")
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RtSpinner(size = RtSpinnerSize.Large, color = MaterialTheme.colorScheme.primary)
            RtSpinner(color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun IconButtonCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "RtIconButton")
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RtIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                onClick = {},
                variant = RtIconButtonVariant.Ink,
            )
            RtIconButton(
                icon = Icons.Rounded.Visibility,
                contentDescription = "Mostrar contraseña",
                onClick = {},
            )
            RtIconButton(
                icon = Icons.Rounded.Delete,
                contentDescription = "Quitar ítem",
                onClick = {},
            )
            RtIconButton(
                icon = Icons.Rounded.Call,
                contentDescription = "Llamar",
                onClick = {},
                variant = RtIconButtonVariant.Tonal,
            )
            RtIconButton(
                icon = Icons.Rounded.Directions,
                contentDescription = "Cómo llegar",
                onClick = {},
                variant = RtIconButtonVariant.Tonal,
            )
        }
    }
}

@Composable
private fun AvatarCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "RtAvatar")
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RtAvatar(name = "Juan Pérez")
            RtAvatar(name = "María Rodríguez")
            RtAvatar(name = "Ana")
        }
    }
}

@Composable
private fun BadgeCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        ComponentTitle(text = "RtBadge")
        Row(
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RtBadge(text = "Ya aceptaste", icon = Icons.Rounded.Check)
            RtBadge(
                text = "3 aceptaciones",
                icon = Icons.Rounded.Group,
                variant = RtBadgeVariant.Neutral,
            )
            RtBadge(text = "Sin aceptaciones aún", variant = RtBadgeVariant.Neutral)
            RtBadge(text = "3", variant = RtBadgeVariant.Count)
        }
    }
}

@Composable
private fun MoleculesSection() {
    Column(
        modifier = Modifier.padding(horizontal = RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
    ) {
        SectionTitle(text = "Moléculas")
        RoleSelectorCatalog()
        InlineAlertCatalog()
        SearchFieldCatalog()
        ContactRowCatalog()
        ConfirmDialogCatalog()
        SnackbarCatalog()
        ErrorStateCatalog()
    }
}

@Composable
private fun RoleSelectorCatalog() {
    var role by remember { mutableStateOf<RtRole?>(RtRole.Neighbor) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtRoleSelector")
        RtRoleSelector(
            selected = role,
            onSelect = {
                role = it
                error = null
            },
            error = error,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(
                text = "Quitar rol",
                onClick = { role = null },
                variant = RtButtonVariant.Secondary,
            )
            RtButton(
                text = "Validar sin rol",
                onClick = {
                    if (role == null) error = "Elige si eres vecino o reciclador"
                },
                variant = RtButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun InlineAlertCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtInlineAlert")
        RtInlineAlert(
            tone = RtInlineAlertTone.Error,
            message = "Correo o contraseña incorrectos.",
        )
        RtInlineAlert(
            tone = RtInlineAlertTone.Warning,
            title = "Juan Pérez podría estar en camino",
            message = "Avísale antes de cancelar: 987 654 321",
        )
        RtInlineAlert(
            tone = RtInlineAlertTone.Info,
            message = "Activa tu ubicación para ver la distancia",
            actionLabel = "Activar ubicación",
            onAction = {},
        )
        RtInlineAlert(
            tone = RtInlineAlertTone.Success,
            message = "El vecino marcará el pedido como Recolectado cuando le entregues el material.",
        )
    }
}

@Composable
private fun SearchFieldCatalog() {
    var basicQuery by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }

    // La feature simula el geocoder con el mismo debounce de 300 ms que HU-04.
    LaunchedEffect(query) {
        if (query.isBlank() || query == selected) {
            suggestions = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(300)
        suggestions = fakeSuggestions(query)
        loading = false
    }

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtSearchField")
        RtSearchField(
            query = basicQuery,
            onQueryChange = { basicQuery = it },
            onSearch = {},
        )
        RtSearchField(
            query = "Calle que no existe",
            onQueryChange = {},
            onSearch = {},
            error = "No encontramos esa dirección. Mueve el mapa a mano.",
        )
        Text(
            text = "Sugerencias",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        RtSearchField(
            query = query,
            onQueryChange = { value ->
                selected = null
                query = value
            },
            onSearch = {
                if (query.isNotBlank()) {
                    suggestions = fakeSuggestions(query)
                    loading = false
                }
            },
            error = if (!loading && query.isNotBlank() && query != selected && suggestions.isEmpty()) {
                "No encontramos esa dirección. Mueve el mapa a mano."
            } else {
                null
            },
            suggestions = suggestions,
            suggestionsLoading = loading,
            onSuggestionSelected = { label ->
                selected = label
                query = label
                suggestions = emptyList()
                loading = false
            },
        )
        Text(
            text = selected?.let { "Elegido: $it" }
                ?: "Toca el buscador y escribe \"Larco\", \"Plaza\" o \"España\"; " +
                "la feature simula el geocoder después de 300 ms.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ContactRowCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtContactRow")
        RtContactRow(
            name = "Juan Pérez",
            subtitle = "987654321",
            phone = "987654321",
            onCall = {},
        )
        RtContactRow(
            name = "María Ríos",
            subtitle = "Aceptó hoy, 10:42",
            phone = "948120337",
            onCall = {},
        )
        RtContactRow(
            name = "María Ríos",
            subtitle = "Esperando confirmación",
        )
    }
}

@Composable
private fun ConfirmDialogCatalog() {
    var dialog by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtConfirmDialog")
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
            RtButton(
                text = "Elegir reciclador",
                onClick = { dialog = 1 },
                variant = RtButtonVariant.Secondary,
            )
            RtButton(
                text = "Cancelar pedido",
                onClick = { dialog = 2 },
                variant = RtButtonVariant.Secondary,
            )
        }
        if (dialog == 1) {
            RtConfirmDialog(
                title = "¿Elegir a Juan Pérez?",
                message = "Él recogerá tu pedido. Los demás recicladores quedarán descartados.",
                confirmLabel = "Elegir",
                onConfirm = { dialog = 0 },
                onDismiss = { dialog = 0 },
            )
        }
        if (dialog == 2) {
            RtConfirmDialog(
                title = "¿Seguro que deseas cancelar?",
                message = "El pedido pasará a tu historial y no se podrá reactivar.",
                confirmLabel = "Sí, cancelar",
                onConfirm = { dialog = 0 },
                onDismiss = { dialog = 0 },
                destructive = true,
                notice = {
                    RtInlineAlert(
                        tone = RtInlineAlertTone.Warning,
                        title = "Juan Pérez podría estar en camino",
                        message = "Avísale antes de cancelar: 987 654 321",
                        actionLabel = "Llamar a Juan",
                        onAction = {},
                    )
                },
            )
        }
    }
}

@Composable
private fun SnackbarCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtSnackbar")
        RtSnackbar(message = "Pedido publicado", actionLabel = "Ver", onAction = {})
        RtSnackbar(
            message = "No se pudo subir una foto",
            actionLabel = "Reintentar",
            onAction = {},
        )
        RtSnackbar(message = "Cambios guardados")
    }
}

@Composable
private fun ErrorStateCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtErrorState")
        RtErrorState(
            title = "No pudimos cargar tus pedidos",
            message = "Revisa tu conexión a internet e inténtalo de nuevo.",
            onRetry = {},
        )
        RtErrorState(
            title = "No pudimos cargar el mapa",
            message = "El servicio no respondió. Inténtalo de nuevo en un momento.",
            onRetry = {},
            kind = RtErrorKind.ServerError,
        )
    }
}

@Composable
private fun OrganismsSection() {
    Column(
        modifier = Modifier.padding(horizontal = RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
    ) {
        SectionTitle(text = "Organismos")
        TopAppBarCatalog()
        BottomNavCatalog()
        MapPickerCatalog()
    }
}

@Composable
private fun TopAppBarCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtTopAppBar")
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

@Composable
private fun BottomNavCatalog() {
    var neighborSelected by remember { mutableStateOf("orders") }
    var recyclerSelected by remember { mutableStateOf("available") }
    val neighborItems = listOf(
        RtBottomNavItem(Icons.AutoMirrored.Rounded.ReceiptLong, "Mis pedidos", "orders"),
        RtBottomNavItem(Icons.Rounded.AddCircle, "Publicar", "publish"),
        RtBottomNavItem(Icons.Rounded.Person, "Perfil", "profile"),
    )
    val recyclerItems = listOf(
        RtBottomNavItem(Icons.Rounded.TravelExplore, "Disponibles", "available"),
        RtBottomNavItem(Icons.Rounded.AssignmentTurnedIn, "Aceptados", "accepted"),
        RtBottomNavItem(Icons.Rounded.Person, "Perfil", "profile"),
    )

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtBottomNav")
        RtBottomNav(
            items = neighborItems,
            selected = neighborSelected,
            onSelect = { neighborSelected = it },
        )
        RtBottomNav(
            items = recyclerItems,
            selected = recyclerSelected,
            onSelect = { recyclerSelected = it },
        )
    }
}

@Composable
private fun MapPickerCatalog() {
    var confirmed by remember { mutableStateOf<RtLatLng?>(null) }
    var cameraCenter by remember { mutableStateOf<RtLatLng?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSuggestion by remember { mutableStateOf<String?>(null) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searchError by remember { mutableStateOf<String?>(null) }

    // Geocoder falso del catálogo, con el debounce de 300 ms de la feature.
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank() || searchQuery == selectedSuggestion) {
            suggestions = emptyList()
            loading = false
            return@LaunchedEffect
        }
        loading = true
        delay(300)
        val found = fakeSuggestions(searchQuery)
        suggestions = found
        loading = false
        searchError = if (found.isEmpty()) "No encontramos esa dirección. Mueve el mapa a mano." else null
    }

    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        ComponentTitle(text = "RtMapPicker")
        RtMapPicker(
            initialCenter = RtLatLng(latitude = -8.1116, longitude = -79.0287),
            onConfirm = { latitude, longitude ->
                confirmed = RtLatLng(latitude = latitude, longitude = longitude)
            },
            center = cameraCenter,
            searchError = searchError,
            onSearchQueryChanged = { value ->
                selectedSuggestion = null
                searchQuery = value
                searchError = null
            },
            searchSuggestions = suggestions,
            searchLoading = loading,
            onSuggestionSelected = { label ->
                selectedSuggestion = label
                searchQuery = label
                cameraCenter = fakeGeocode(label)
                suggestions = emptyList()
                loading = false
                searchError = null
            },
            onSearch = { value ->
                selectedSuggestion = null
                searchQuery = value
                val found = fakeSuggestions(value)
                suggestions = found
                loading = false
                searchError = if (value.isNotBlank() && found.isEmpty()) {
                    "No encontramos esa dirección. Mueve el mapa a mano."
                } else {
                    null
                }
            },
        )
        // Demuestra el `center` controlado: simula el resultado de una búsqueda sin
        // acoplar el organismo al Geocoder (la feature es la que busca).
        Row(horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
            RtButton(
                text = "Simular búsqueda",
                onClick = {
                    selectedSuggestion = "Av. Larco 1250"
                    searchQuery = "Av. Larco 1250"
                    cameraCenter = fakeGeocode("Av. Larco 1250, Víctor Larco Herrera")
                    suggestions = emptyList()
                    loading = false
                    searchError = null
                },
                variant = RtButtonVariant.Secondary,
                modifier = Modifier.weight(1f),
            )
            RtButton(
                text = "Volver al centro",
                onClick = { cameraCenter = RtLatLng(latitude = -8.1116, longitude = -79.0287) },
                variant = RtButtonVariant.Text,
                modifier = Modifier.weight(1f),
            )
        }
        Text(
            text = confirmed?.let {
                "Centro confirmado: %.4f, %.4f".format(it.latitude, it.longitude)
            } ?: "Escribe \"Larco\", \"Plaza\" o \"España\" en el buscador; elige una " +
                "sugerencia o mueve el mapa y confirma.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Direcciones del geocoder falso del catálogo: simula la feature de HU-04. */
private val FakeAddresses = listOf(
    "Av. Larco 1250, Víctor Larco Herrera" to RtLatLng(latitude = -8.1122, longitude = -79.0274),
    "Plaza de Armas, Trujillo" to RtLatLng(latitude = -8.1119, longitude = -79.0288),
    "Av. España 500, Trujillo" to RtLatLng(latitude = -8.1108, longitude = -79.0259),
    "Av. Larco 1302, Víctor Larco Herrera" to RtLatLng(latitude = -8.1130, longitude = -79.0270),
)

private fun fakeSuggestions(query: String): List<String> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return emptyList()
    return FakeAddresses
        .map { it.first }
        .filter { label -> label.contains(normalized, ignoreCase = true) }
        .take(4)
}

private fun fakeGeocode(label: String): RtLatLng? =
    FakeAddresses.firstOrNull { it.first == label }?.second

@Composable
private fun ActionBarCatalog() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s3)) {
        Column(modifier = Modifier.padding(horizontal = RtSpacing.s4)) {
            ComponentTitle(text = "RtActionBar")
        }
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
    }
}
