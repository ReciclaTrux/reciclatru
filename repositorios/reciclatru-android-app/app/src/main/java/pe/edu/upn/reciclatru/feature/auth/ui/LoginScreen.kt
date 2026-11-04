package pe.edu.upn.reciclatru.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Recycling
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtRadius
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSize
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

@Composable
fun LoginScreen(
    onLoggedIn: (UserRole) -> Unit,
    onGoToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.loggedInRole) {
        state.loggedInRole?.let(onLoggedIn)
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onSubmit = viewModel::onSubmit,
        onGoToRegister = onGoToRegister,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onGoToRegister: () -> Unit,
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
                    text = if (state.submitting) "Ingresando…" else "Ingresar",
                    onClick = onSubmit,
                    loading = state.submitting,
                    fullWidth = true,
                    modifier = Modifier.testTag("btn_ingresar"),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "¿Aún no tienes cuenta?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    RtButton(
                        text = "Regístrate",
                        onClick = onGoToRegister,
                        variant = RtButtonVariant.Text,
                        size = RtButtonSize.Compact,
                        enabled = !state.submitting,
                        modifier = Modifier.testTag("btn_ir_a_registro"),
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
            Box(
                modifier = Modifier
                    .padding(top = RtSpacing.s5)
                    .size(RtSize.thumbSm)
                    .clip(RoundedCornerShape(RtRadius.md))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Recycling,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(RtSize.iconMd),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(RtSpacing.s1)) {
                Text(
                    text = "Hola de nuevo",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Ingresa con tu correo y contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            when {
                state.generalError != null -> RtInlineAlert(
                    tone = RtInlineAlertTone.Error,
                    message = state.generalError,
                    modifier = Modifier.testTag("alert_login_error"),
                )
                state.sessionExpired -> RtInlineAlert(
                    tone = RtInlineAlertTone.Info,
                    message = "Tu sesión expiró. Ingresa de nuevo para continuar.",
                    modifier = Modifier.testTag("alert_sesion_expirada"),
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
                readOnly = state.submitting,
                modifier = Modifier.testTag("input_correo"),
            )
            RtTextField(
                label = "Contraseña",
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = "Tu contraseña",
                error = state.passwordError,
                leadingIcon = Icons.Rounded.Lock,
                keyboardType = KeyboardType.Password,
                readOnly = state.submitting,
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
        }
    }
}

@Preview(name = "Credenciales incorrectas", showBackground = true, heightDp = 760)
@Composable
private fun LoginErrorPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoginContent(
                state = LoginUiState(
                    email = "ana.torres@gmail.com",
                    password = "reciclo",
                    generalError = "Correo o contraseña incorrectos.",
                ),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onSubmit = {},
                onGoToRegister = {},
            )
        }
    }
}

@Preview(name = "Sesión expirada", showBackground = true, heightDp = 760)
@Composable
private fun LoginExpiredPreview() {
    RtTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            LoginContent(
                state = LoginUiState(email = "ana.torres@gmail.com", sessionExpired = true, submitting = true),
                onEmailChange = {},
                onPasswordChange = {},
                onTogglePasswordVisibility = {},
                onSubmit = {},
                onGoToRegister = {},
            )
        }
    }
}
