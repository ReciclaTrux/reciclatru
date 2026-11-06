package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.profile.domain.PickupAddress
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtAvatar
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtBadge
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtSnackbarHost
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTextStyles
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onChoosePickupPoint: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }
    LifecycleResumeEffect(Unit) {
        viewModel.onReturnFromPickupPoint()
        onPauseOrDispose { }
    }

    ProfileContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onFullNameChange = viewModel::onFullNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onChoosePickupPoint = onChoosePickupPoint,
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
        onLogout = onLogout,
        modifier = modifier,
    )
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onChoosePickupPoint: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Perfil", onBack = onBack) },
        snackbarHost = { RtSnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.loading -> Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                RtSpinner(size = RtSpinnerSize.Large)
            }
            state.loadError != null -> RtErrorState(
                title = "No pudimos cargar tu perfil",
                message = state.loadError,
                onRetry = onRetry,
                modifier = Modifier.padding(innerPadding),
            )
            else -> Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(RtSpacing.s4),
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
            ) {
                ProfileHeader(state = state)
                state.saveError?.let { message ->
                    RtInlineAlert(
                        tone = RtInlineAlertTone.Error,
                        message = message,
                        modifier = Modifier.testTag("alert_perfil_error"),
                    )
                }
                FullNameField(state = state, onChange = onFullNameChange)
                PhoneField(
                    state = state,
                    onChange = onPhoneChange,
                    helper = if (state.role == UserRole.Recycler) {
                        "Si lo cambias, los vecinos con pedidos activos verán el número nuevo"
                    } else {
                        null
                    },
                )
                if (state.role == UserRole.Neighbor) {
                    PickupPointSection(
                        address = state.pickupAddress,
                        reference = state.reference,
                        onChoose = onChoosePickupPoint,
                    )
                }
                RtTextField(
                    label = "Correo y rol",
                    value = listOfNotNull(state.email, state.role?.label()).joinToString(" · "),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.testTag("input_correo_rol"),
                )
                RtButton(
                    text = if (state.saving) "Guardando…" else "Guardar cambios",
                    onClick = onSave,
                    variant = RtButtonVariant.Secondary,
                    loading = state.saving,
                    fullWidth = true,
                    modifier = Modifier.testTag("btn_guardar_cambios"),
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RtButton(
                        text = "Cerrar sesión",
                        onClick = onLogout,
                        variant = RtButtonVariant.Text,
                        icon = Icons.AutoMirrored.Rounded.Logout,
                        modifier = Modifier.testTag("btn_cerrar_sesion"),
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(state: ProfileUiState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(RtSpacing.s3),
    ) {
        RtAvatar(name = state.savedFullName.ifBlank { state.email })
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.savedFullName.ifBlank { "Sin nombre" },
                style = RtTextStyles.bodyStrong,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        state.role?.let { role ->
            RtBadge(
                text = role.label(),
                icon = if (role == UserRole.Neighbor) Icons.Rounded.Home else Icons.Rounded.Recycling,
            )
        }
    }
}

@Preview(name = "Perfil del vecino", showBackground = true, heightDp = 760)
@Composable
private fun ProfileNeighborPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            ProfileContent(
                state = ProfileUiState(
                    loading = false,
                    email = "ana.torres@gmail.com",
                    role = UserRole.Neighbor,
                    savedFullName = "Ana Torres",
                    fullName = "Ana Torres",
                    phone = "948120337",
                    reference = "Dpto. 302, puerta negra",
                    pickupAddress = PickupAddress(address = "Av. Larco 1250", district = "Víctor Larco Herrera"),
                ),
                snackbarHostState = remember { SnackbarHostState() },
                onBack = {},
                onFullNameChange = {},
                onPhoneChange = {},
                onChoosePickupPoint = {},
                onSave = {},
                onRetry = {},
                onLogout = {},
            )
        }
    }
}
