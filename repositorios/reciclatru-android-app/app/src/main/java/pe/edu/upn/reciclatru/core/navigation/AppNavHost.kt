package pe.edu.upn.reciclatru.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.reciclatru.core.health.ui.HealthUiState
import pe.edu.upn.reciclatru.core.health.ui.HealthViewModel
import pe.edu.upn.reciclatru.core.session.SignOutReason
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.ui.LoginScreen
import pe.edu.upn.reciclatru.feature.auth.ui.LogoutViewModel
import pe.edu.upn.reciclatru.feature.auth.ui.RegisterScreen
import pe.edu.upn.reciclatru.feature.profile.ui.CompleteProfileScreen
import pe.edu.upn.reciclatru.feature.profile.ui.ProfileScreen
import pe.edu.upn.reciclatru.feature.profile.ui.LocationScreen
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButton
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtButtonVariant
import pe.edu.upn.reciclatru.ui.designsystem.atoms.RtSpinner
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlert
import pe.edu.upn.reciclatru.ui.designsystem.molecules.RtInlineAlertTone
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtSpacing
import pe.edu.upn.reciclatru.ui.designsystem.theme.RtTheme

/**
 * Único `NavHost` de la app. Cada feature registra sus pantallas acá cuando su HU lo
 * necesita; las pantallas no se conocen entre sí.
 *
 * Arranca en [AppRoutes.Start] y, según haya sesión guardada, va al inicio o al ingreso.
 * Al cerrar sesión o al vencer el token, vuelve al ingreso sin historial.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: AppSessionViewModel = hiltViewModel(),
) {
    val startDestination by sessionViewModel.startDestination.collectAsStateWithLifecycle()
    val role by sessionViewModel.role.collectAsStateWithLifecycle()

    LaunchedEffect(navController) {
        sessionViewModel.signedOut.collect { reason ->
            navController.navigate(AppRoutes.login(expired = reason == SignOutReason.Expired)) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    val goHome: () -> Unit = {
        navController.navigate(AppRoutes.Home) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }
    val goCompleteProfile: () -> Unit = {
        navController.navigate(AppRoutes.CompleteProfile) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoutes.Start,
        modifier = modifier,
    ) {
        composable(AppRoutes.Start) {
            LaunchedEffect(startDestination) {
                startDestination?.let { destination ->
                    navController.navigate(destination) {
                        popUpTo(AppRoutes.Start) { inclusive = true }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize())
        }
        composable(
            route = AppRoutes.Login,
            arguments = listOf(
                navArgument(AppRoutes.ExpiredArg) {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) {
            LoginScreen(
                onLoggedIn = { needsProfile -> if (needsProfile) goCompleteProfile() else goHome() },
                onGoToRegister = {
                    navController.navigate(AppRoutes.Register) {
                        popUpTo(AppRoutes.Login) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoutes.Home) {
            val healthViewModel: HealthViewModel = hiltViewModel()
            val healthState by healthViewModel.uiState.collectAsStateWithLifecycle()
            HomePlaceholder(
                healthState = healthState,
                onRetryHealth = healthViewModel::onRetry,
                showPickupLocation = role == UserRole.Neighbor,
                onOpenProfile = { navController.navigate(AppRoutes.Profile) },
                onOpenPickupLocation = { navController.navigate(AppRoutes.PickupLocation) },
            )
        }
        composable(AppRoutes.Register) {
            RegisterScreen(
                onRegistered = { goCompleteProfile() },
                onGoToLogin = {
                    navController.navigate(AppRoutes.login()) {
                        popUpTo(AppRoutes.Register) { inclusive = true }
                    }
                },
            )
        }
        composable(AppRoutes.CompleteProfile) {
            CompleteProfileScreen(
                onCompleted = { goHome() },
                onChoosePickupPoint = { navController.navigate(AppRoutes.PickupLocation) },
            )
        }
        composable(AppRoutes.Profile) {
            val logoutViewModel: LogoutViewModel = hiltViewModel()
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onChoosePickupPoint = { navController.navigate(AppRoutes.PickupLocation) },
                onLogout = logoutViewModel::onLogout,
            )
        }
        composable(AppRoutes.PickupLocation) {
            LocationScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() },
            )
        }
    }
}

/**
 * Pantalla temporal de arranque. No es una HU: existe para que la app compile y se pueda
 * abrir la única HU implementada (HU-04) mientras el equipo hace el registro y el ingreso.
 */
@Composable
private fun HomePlaceholder(
    healthState: HealthUiState,
    onRetryHealth: () -> Unit,
    showPickupLocation: Boolean,
    onOpenProfile: () -> Unit,
    onOpenPickupLocation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(RtSpacing.s4),
        verticalArrangement = Arrangement.spacedBy(RtSpacing.s4, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ReciclaTru",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = "Conectamos a los vecinos de Trujillo con recicladores.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        BackendHealthStatus(state = healthState, onRetry = onRetryHealth)
        RtInlineAlert(
            tone = RtInlineAlertTone.Info,
            message = "Esta es la base de la app. Los pedidos llegan en el Sprint 2.",
        )
        if (showPickupLocation) {
            RtButton(
                text = "Marcar punto de recojo",
                onClick = onOpenPickupLocation,
                fullWidth = true,
                modifier = Modifier.testTag("btn_abrir_ubicacion"),
            )
        }
        RtButton(
            text = "Mi perfil",
            onClick = onOpenProfile,
            variant = RtButtonVariant.Secondary,
            fullWidth = true,
            modifier = Modifier.testTag("btn_abrir_perfil"),
        )
    }
}

@Composable
private fun BackendHealthStatus(state: HealthUiState, onRetry: () -> Unit) {
    when {
        state.checking -> RtSpinner(modifier = Modifier.testTag("spinner_conexion_backend"))
        state.connected -> RtInlineAlert(
            tone = RtInlineAlertTone.Success,
            message = "Conectado",
            modifier = Modifier.testTag("alert_conexion_backend"),
        )
        else -> RtInlineAlert(
            tone = RtInlineAlertTone.Error,
            message = state.error.orEmpty(),
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = Modifier.testTag("alert_conexion_backend"),
        )
    }
}

@Preview(name = "Inicio", showBackground = true, heightDp = 700)
@Composable
private fun HomePlaceholderPreview() {
    RtTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            HomePlaceholder(
                healthState = HealthUiState(checking = false, connected = true),
                onRetryHealth = {},
                showPickupLocation = true,
                onOpenProfile = {},
                onOpenPickupLocation = {},
            )
        }
    }
}
