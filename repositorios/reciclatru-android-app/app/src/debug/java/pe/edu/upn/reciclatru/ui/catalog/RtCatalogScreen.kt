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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Directions
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s5),
        ) {
            item { CatalogHeader() }
            item { ThemeSection() }
            item { AtomsSection() }
        }
    }
}

@Composable
private fun CatalogHeader() {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s1)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s5)) {
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
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s5)) {
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
