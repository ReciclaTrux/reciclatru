package pe.edu.upn.reciclatru.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import pe.edu.upn.reciclatru.core.session.SignOutReason
import pe.edu.upn.reciclatru.core.session.UserRole
import pe.edu.upn.reciclatru.feature.auth.ui.LoginScreen
import pe.edu.upn.reciclatru.feature.auth.ui.LogoutViewModel
import pe.edu.upn.reciclatru.feature.auth.ui.RegisterScreen
import pe.edu.upn.reciclatru.feature.explore.ui.AvailableOrdersScreen
import pe.edu.upn.reciclatru.feature.orders.ui.CreateOrderScreen
import pe.edu.upn.reciclatru.feature.orders.ui.MyOrdersScreen
import pe.edu.upn.reciclatru.feature.orders.ui.OrderDetailScreen
import pe.edu.upn.reciclatru.feature.profile.ui.CompleteProfileScreen
import pe.edu.upn.reciclatru.feature.profile.ui.ProfileScreen
import pe.edu.upn.reciclatru.feature.profile.ui.LocationScreen

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
        composable(AppRoutes.Home) { entry ->
            when (role) {
                UserRole.Neighbor -> {
                    val publishedOrderId by entry.savedStateHandle
                        .getStateFlow<String?>(AppRoutes.OrderPublishedResult, null)
                        .collectAsStateWithLifecycle()
                    MyOrdersScreen(
                        publishedOrderId = publishedOrderId,
                        onOrderPublishedHandled = { entry.savedStateHandle[AppRoutes.OrderPublishedResult] = null },
                        onPublish = { navController.navigate(AppRoutes.CreateOrder) },
                        onOpenProfile = { navController.navigate(AppRoutes.Profile) },
                        onOpenOrder = { orderId -> navController.navigate(AppRoutes.orderDetail(orderId)) },
                    )
                }
                UserRole.Recycler -> AvailableOrdersScreen(
                    onOpenProfile = { navController.navigate(AppRoutes.Profile) },
                )
                null -> Box(modifier = Modifier.fillMaxSize())
            }
        }
        composable(
            route = AppRoutes.OrderDetail,
            arguments = listOf(navArgument(AppRoutes.OrderIdArg) { type = NavType.StringType }),
        ) {
            OrderDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(AppRoutes.CreateOrder) {
            CreateOrderScreen(
                onBack = { navController.popBackStack() },
                onPublished = { orderId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AppRoutes.OrderPublishedResult, orderId)
                    navController.popBackStack()
                },
                onChoosePickupPoint = { navController.navigate(AppRoutes.PickupLocation) },
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
