package pe.edu.upn.reciclatru.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinnerSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtErrorState
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtActionBar
import pe.edu.upn.reciclatru.ui.designsystem.organisms.RtTopAppBar
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@Composable
fun CompleteProfileScreen(
    onCompleted: () -> Unit,
    onChoosePickupPoint: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.savedOnce) {
        if (state.savedOnce) onCompleted()
    }
    LifecycleResumeEffect(Unit) {
        viewModel.onReturnFromPickupPoint()
        onPauseOrDispose { }
    }

    CompleteProfileContent(
        state = state,
        onFullNameChange = viewModel::onFullNameChange,
        onPhoneChange = viewModel::onPhoneChange,
        onReferenceChange = viewModel::onReferenceChange,
        onChoosePickupPoint = onChoosePickupPoint,
        onSave = viewModel::onSave,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}

@Composable
private fun CompleteProfileContent(
    state: ProfileUiState,
    onFullNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onReferenceChange: (String) -> Unit,
    onChoosePickupPoint: () -> Unit,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RtTopAppBar(title = "Completa tu perfil") },
        bottomBar = {
            if (!state.loading && state.loadError == null) {
                RtActionBar(
                    primary = {
                        RtButton(
                            text = if (state.saving) "Guardando…" else "Guardar perfil",
                            onClick = onSave,
                            loading = state.saving,
                            fullWidth = true,
                            modifier = Modifier.testTag("btn_guardar_perfil"),
                        )
                    },
                )
            }
        },
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
                Text(
                    text = if (state.role == UserRole.Recycler) {
                        "El vecino verá tu nombre y celular cuando aceptes su pedido."
                    } else {
                        "Así el reciclador sabrá quién eres y cómo llegar a ti."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
                    helper = if (state.role == UserRole.Recycler) "9 dígitos, empieza con 9" else null,
                )
                if (state.role == UserRole.Neighbor) {
                    PickupPointSection(
                        address = state.pickupAddress,
                        reference = null,
                        onChoose = onChoosePickupPoint,
                    )
                    RtTextField(
                        label = "Referencia (opcional)",
                        value = state.reference,
                        onValueChange = onReferenceChange,
                        placeholder = "Dpto. 302, puerta negra, frente al parque",
                        helper = "Ayuda al reciclador a encontrar tu puerta",
                        maxLength = 150,
                        multiline = true,
                        modifier = Modifier.testTag("input_referencia"),
                    )
                } else {
                    RtInlineAlert(
                        tone = RtInlineAlertTone.Info,
                        message = "No necesitas registrar una dirección. Para ordenar los pedidos por " +
                            "distancia usaremos la ubicación de tu teléfono, sin guardarla.",
                    )
                }
            }
        }
    }
}

@Preview(name = "Completar perfil vecino", showBackground = true, heightDp = 760)
@Composable
private fun CompleteProfileNeighborPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CompleteProfileContent(
                state = ProfileUiState(
                    loading = false,
                    role = UserRole.Neighbor,
                    fullName = "Ana Torres",
                    phone = "94812",
                    phoneError = "Debe tener 9 dígitos y empezar con 9",
                ),
                onFullNameChange = {},
                onPhoneChange = {},
                onReferenceChange = {},
                onChoosePickupPoint = {},
                onSave = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Completar perfil reciclador", showBackground = true, heightDp = 760)
@Composable
private fun CompleteProfileRecyclerPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            CompleteProfileContent(
                state = ProfileUiState(
                    loading = false,
                    role = UserRole.Recycler,
                    fullName = "Juan Pérez",
                    phone = "987654321",
                ),
                onFullNameChange = {},
                onPhoneChange = {},
                onReferenceChange = {},
                onChoosePickupPoint = {},
                onSave = {},
                onRetry = {},
            )
        }
    }
}
