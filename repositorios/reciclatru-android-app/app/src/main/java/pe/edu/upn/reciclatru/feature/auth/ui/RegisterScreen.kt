package pe.edu.upn.reciclatru.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonSize
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtTextField
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtRole
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtRoleSelector
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@Composable
fun RegisterScreen(
    onRegistered: (UserRole) -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.registeredRole) {
        state.registeredRole?.let(onRegistered)
    }

    RegisterContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onRoleSelect = viewModel::onRoleSelect,
        onSubmit = viewModel::onSubmit,
        onGoToLogin = onGoToLogin,
        modifier = modifier,
    )
}

@Composable
private fun RegisterContent(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onRoleSelect: (UserRole) -> Unit,
    onSubmit: () -> Unit,
    onGoToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(horizontal = RtSpacing.s4, vertical = RtSpacing.s3),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(RtSpacing.s1),
            ) {
                RtButton(
                    text = if (state.submitting) "Creando cuenta…" else "Crear cuenta",
                    onClick = onSubmit,
                    loading = state.submitting,
                    fullWidth = true,
                    modifier = Modifier.testTag("btn_crear_cuenta"),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¿Ya tienes cuenta?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    RtButton(
                        text = "Inicia sesión",
                        onClick = onGoToLogin,
                        variant = RtButtonVariant.Text,
                        size = RtButtonSize.Compact,
                        enabled = !state.submitting,
                        modifier = Modifier.testTag("btn_ir_a_login"),
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(RtSpacing.s4),
            verticalArrangement = Arrangement.spacedBy(RtSpacing.s4),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s1)) {
                Text(
                    text = "ReciclaTru",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = "Crea tu cuenta",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Conecta tu material reciclable con quien lo recoge.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            state.generalError?.let { message ->
                RtInlineAlert(
                    tone = RtInlineAlertTone.Error,
                    message = message,
                    modifier = Modifier.testTag("alert_registro_error"),
                )
            }
            RtTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = "nombre@correo.com",
                error = state.emailError,
                leadingIcon = Icons.Rounded.Mail,
                keyboardType = KeyboardType.Email,
                modifier = Modifier.testTag("input_correo"),
            )
            RtTextField(
                label = "Contraseña",
                value = state.password,
                onValueChange = onPasswordChange,
                helper = "Mínimo 8 caracteres",
                error = state.passwordError,
                leadingIcon = Icons.Rounded.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (state.passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = if (state.passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                trailingIconDescription = if (state.passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
                onTrailingIconClick = onTogglePasswordVisibility,
                modifier = Modifier.testTag("input_contrasena"),
            )
            RtRoleSelector(
                selected = state.role?.toRtRole(),
                onSelect = { role -> onRoleSelect(role.toUserRole()) },
                error = state.roleError,
                modifier = Modifier.testTag("selector_rol"),
            )
        }
    }
}

private fun UserRole.toRtRole(): RtRole = when (this) {
    UserRole.Neighbor -> RtRole.Neighbor
    UserRole.Recycler -> RtRole.Recycler
}

private fun RtRole.toUserRole(): UserRole = when (this) {
    RtRole.Neighbor -> UserRole.Neighbor
    RtRole.Recycler -> UserRole.Recycler
}

@Preview(name = "Registro", showBackground = true, heightDp = 800)
@Composable
private fun RegisterPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RegisterContent(
                state = RegisterUiState(email = "ana.torres@gmail.com", password = "reciclo2026", role = UserRole.Neighbor),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onRoleSelect = {},
                onSubmit = {},
                onGoToLogin = {},
            )
        }
    }
}

@Preview(name = "Correo ya registrado", showBackground = true, heightDp = 800)
@Composable
private fun RegisterEmailTakenPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            RegisterContent(
                state = RegisterUiState(
                    email = "juan.perez@gmail.com",
                    password = "recojo2026",
                    passwordVisible = true,
                    role = UserRole.Recycler,
                    emailError = "Ya existe una cuenta con este correo",
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onRoleSelect = {},
                onSubmit = {},
                onGoToLogin = {},
            )
        }
    }
}
