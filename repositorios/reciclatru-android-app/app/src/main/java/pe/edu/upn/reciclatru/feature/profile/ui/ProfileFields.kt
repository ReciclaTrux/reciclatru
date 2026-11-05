package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles

internal fun UserRole.label(): String = when (this) {
    UserRole.Neighbor -> "Vecino"
    UserRole.Recycler -> "Reciclador"
}

@Composable
internal fun FullNameField(state: ProfileUiState, onChange: (String) -> Unit) {
    RtTextField(
        label = "Nombre y apellido",
        value = state.fullName,
        onValueChange = onChange,
        placeholder = "Ana Torres",
        error = state.fullNameError,
        leadingIcon = Icons.Rounded.Person,
        modifier = Modifier.testTag("input_nombre"),
    )
}

@Composable
internal fun PhoneField(state: ProfileUiState, onChange: (String) -> Unit, helper: String?) {
    RtTextField(
        label = "Celular",
        value = state.phone,
        onValueChange = onChange,
        placeholder = "987654321",
        helper = helper,
        error = state.phoneError,
        leadingIcon = Icons.Rounded.Smartphone,
        keyboardType = KeyboardType.Phone,
        modifier = Modifier.testTag("input_celular"),
    )
}

@Composable
internal fun PickupPointSection(address: PickupAddress?, reference: String?, onChoose: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s2)) {
        Text(
            text = "Punto de recojo",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (address == null) {
            RtButton(
                text = "Elegir ubicación en el mapa",
                onClick = onChoose,
                variant = RtButtonVariant.Secondary,
                icon = Icons.Rounded.Place,
                fullWidth = true,
                modifier = Modifier.testTag("btn_elegir_ubicacion"),
            )
            Text(
                text = "Es donde el reciclador recogerá tu material",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RtSpacing.s2),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = address.address.ifBlank { "Punto marcado en el mapa" },
                        style = RtTextStyles.bodyStrong,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    val detail = listOf(address.district, reference.orEmpty())
                        .filter { it.isNotBlank() }
                        .joinToString(" · ")
                    if (detail.isNotEmpty()) {
                        Text(
                            text = detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                RtButton(
                    text = "Cambiar",
                    onClick = onChoose,
                    variant = RtButtonVariant.Text,
                    size = RtButtonSize.Compact,
                    modifier = Modifier.testTag("btn_cambiar_ubicacion"),
                )
            }
        }
    }
}
