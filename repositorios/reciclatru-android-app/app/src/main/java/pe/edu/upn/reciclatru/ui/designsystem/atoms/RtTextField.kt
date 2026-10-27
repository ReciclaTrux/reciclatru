package pe.edu.upn.reciclatru.ui.designsystem.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.PhoneIphone
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtStroke
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme
import pe.edu.upn.reciclatru.ui.designsystem.theme.rtColors

/**
 * Campo de texto con etiqueta arriba y ayuda o error abajo.
 *
 * @param placeholder ejemplo real del dato que se espera, sin "ej.".
 * @param helper ayuda bajo el campo; [error] la reemplaza cuando existe.
 * @param error texto que dice qué corregir, en una línea.
 * @param leadingIcon ícono al inicio del campo.
 * @param suffix texto fijo al final del valor (por ejemplo "kg").
 * @param maxLength límite de caracteres; muestra el contador a la derecha.
 * @param readOnly campo de solo lectura: fondo `surface-variant` y candado.
 * @param multiline variante de texto largo: alto mínimo de 96 px y el texto se ajusta.
 * @param visualTransformation transforma lo que se muestra sin cambiar el valor, por
 *   ejemplo para ocultar la contraseña.
 * @param trailingIcon acción al final del campo, por ejemplo mostrar u ocultar la
 *   contraseña; requiere [trailingIconDescription] y [onTrailingIconClick].
 */
@Composable
fun RtTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    helper: String? = null,
    error: String? = null,
    leadingIcon: ImageVector? = null,
    suffix: String? = null,
    maxLength: Int? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false,
    multiline: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: ImageVector? = null,
    trailingIconDescription: String? = null,
    onTrailingIconClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val rtColors = MaterialTheme.rtColors
    val shape = RoundedCornerShape(RtRadius.md)
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val borderColor = when {
        error != null -> colorScheme.error
        readOnly -> colorScheme.outlineVariant
        focused -> colorScheme.primary
        else -> colorScheme.outline
    }
    val containerColor = if (readOnly) colorScheme.surfaceVariant else colorScheme.surface
    val textStyle = MaterialTheme.typography.bodyLarge.copy(color = colorScheme.onSurface)
    val helpText = error ?: helper

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = colorScheme.onSurface,
        )
        Row(
            modifier = Modifier
                .padding(top = RtSpacing.s1)
                .fillMaxWidth()
                .heightIn(min = if (multiline) RtSize.inputHeightMultiline else RtSize.inputHeight)
                .clip(shape)
                .background(containerColor)
                .border(RtStroke.control, borderColor, shape)
                .then(if (multiline) Modifier.padding(vertical = RtSpacing.s3) else Modifier)
                .padding(start = RtSpacing.s4, end = RtSpacing.s2),
            verticalAlignment = if (multiline) Alignment.Top else Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = colorScheme.onSurfaceVariant,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { newValue ->
                    onValueChange(if (maxLength != null) newValue.take(maxLength) else newValue)
                },
                modifier = Modifier.weight(1f),
                readOnly = readOnly,
                textStyle = textStyle,
                cursorBrush = SolidColor(colorScheme.primary),
                singleLine = !multiline,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                visualTransformation = visualTransformation,
                interactionSource = interactionSource,
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty() && placeholder != null) {
                            Text(
                                text = placeholder,
                                style = textStyle,
                                color = rtColors.inkSubtle,
                                maxLines = if (multiline) Int.MAX_VALUE else 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (suffix != null) {
                Text(
                    text = suffix,
                    style = RtTextStyles.bodyStrong,
                    color = colorScheme.onSurfaceVariant,
                )
            }
            if (readOnly) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(RtSize.iconMd),
                    tint = colorScheme.onSurfaceVariant,
                )
            }
            if (trailingIcon != null && trailingIconDescription != null && onTrailingIconClick != null) {
                RtIconButton(
                    icon = trailingIcon,
                    contentDescription = trailingIconDescription,
                    onClick = onTrailingIconClick,
                )
            }
        }
        if (helpText != null || maxLength != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = RtSpacing.s1),
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(RtSpacing.s1),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (error != null) {
                        Icon(
                            imageVector = Icons.Rounded.Error,
                            contentDescription = null,
                            modifier = Modifier.size(RtSize.iconCaption),
                            tint = colorScheme.error,
                        )
                    }
                    if (helpText != null) {
                        Text(
                            text = helpText,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (error != null) colorScheme.error else colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (maxLength != null) {
                    Text(
                        text = "${value.length}/$maxLength",
                        style = MaterialTheme.typography.labelMedium,
                        color = colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(name = "Variantes y estados", showBackground = true, heightDp = 620)
@Composable
private fun RtTextFieldPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                RtTextField(
                    label = "Correo electrónico",
                    value = "",
                    onValueChange = {},
                    placeholder = "nombre@correo.com",
                    leadingIcon = Icons.Rounded.Mail,
                    keyboardType = KeyboardType.Email,
                )
                RtTextField(
                    label = "Contraseña",
                    value = "••••••••",
                    onValueChange = {},
                    helper = "Mínimo 8 caracteres",
                    leadingIcon = Icons.Rounded.Lock,
                    keyboardType = KeyboardType.Password,
                    trailingIcon = Icons.Rounded.Visibility,
                    trailingIconDescription = "Mostrar contraseña",
                    onTrailingIconClick = {},
                )
                RtTextField(
                    label = "Celular",
                    value = "94812",
                    onValueChange = {},
                    error = "Debe tener 9 dígitos y empezar con 9",
                    leadingIcon = Icons.Rounded.PhoneIphone,
                    keyboardType = KeyboardType.Phone,
                )
                RtTextField(
                    label = "Peso",
                    value = "2.5",
                    onValueChange = {},
                    suffix = "kg",
                    keyboardType = KeyboardType.Decimal,
                )
                RtTextField(
                    label = "Referencia (opcional)",
                    value = "Dpto. 302, puerta negra, frente al parque",
                    onValueChange = {},
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
    }
}

@Preview(name = "Texto largo vacío", showBackground = true)
@Composable
private fun RtTextFieldEmptyMultilinePreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(RtSpacing.s4)) {
                RtTextField(
                    label = "Referencia (opcional)",
                    value = "",
                    onValueChange = {},
                    placeholder = "Dpto. 302, puerta negra, frente al parque",
                    helper = "Ayuda al reciclador a encontrar tu puerta",
                    maxLength = 150,
                    multiline = true,
                )
            }
        }
    }
}
