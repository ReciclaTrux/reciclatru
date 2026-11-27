package pe.edu.upn.reciclatru.view.navigation



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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pe.edu.upn.reciclatru.model.session.SignOutReason
import pe.edu.upn.reciclatru.model.session.UserRole
import pe.edu.upn.reciclatru.view.auth.LoginScreen
import pe.edu.upn.reciclatru.view.auth.RegisterScreen
import pe.edu.upn.reciclatru.view.explore.AvailableOrderDetailScreen
import pe.edu.upn.reciclatru.view.explore.AvailableOrdersScreen
import pe.edu.upn.reciclatru.view.explore.MyAcceptancesScreen
import pe.edu.upn.reciclatru.view.orders.CreateOrderScreen
import pe.edu.upn.reciclatru.view.orders.EditOrderScreen
import pe.edu.upn.reciclatru.view.orders.MyOrdersScreen
import pe.edu.upn.reciclatru.view.orders.OrderDetailScreen
import pe.edu.upn.reciclatru.view.profile.CompleteProfileScreen
import pe.edu.upn.reciclatru.view.profile.LocationScreen
import pe.edu.upn.reciclatru.view.profile.ProfileScreen
import pe.edu.upn.reciclatru.viewmodel.auth.LogoutViewModel
import pe.edu.upn.reciclatru.viewmodel.session.AppSessionViewModel



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
                UserRole.Recycler -> {
                    val orderUnavailable by entry.savedStateHandle
                        .getStateFlow(AppRoutes.OrderUnavailableResult, false)
                        .collectAsStateWithLifecycle()
                    AvailableOrdersScreen(
                        onOpenAccepted = {
                            navController.navigate(AppRoutes.MyAcceptances) { launchSingleTop = true }
                        },
                        onOpenProfile = { navController.navigate(AppRoutes.Profile) },
                        onOpenOrder = { orderId -> navController.navigate(AppRoutes.availableOrderDetail(orderId)) },
                        orderUnavailable = orderUnavailable,
                        onOrderUnavailableHandled = { entry.savedStateHandle[AppRoutes.OrderUnavailableResult] = false },
                    )
                }
                null -> Box(modifier = Modifier.fillMaxSize())
            }
        }
        composable(
            route = AppRoutes.OrderDetail,
            arguments = listOf(navArgument(AppRoutes.OrderIdArg) { type = NavType.StringType }),
        ) {
            OrderDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { orderId -> navController.navigate(AppRoutes.editOrder(orderId)) },
            )
        }
        composable(
            route = AppRoutes.EditOrder,
            arguments = listOf(navArgument(AppRoutes.OrderIdArg) { type = NavType.StringType }),
        ) {
            EditOrderScreen(
                onBack = { navController.popBackStack() },
                onFinished = { message ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AppRoutes.OrderEditResult, message)
                    navController.popBackStack()
                },
            )
        }
        composable(AppRoutes.MyAcceptances) {
            MyAcceptancesScreen(
                onOpenAvailable = { navController.popBackStack(AppRoutes.Home, inclusive = false) },
                onOpenProfile = { navController.navigate(AppRoutes.Profile) },
                onOpenOrder = { orderId -> navController.navigate(AppRoutes.availableOrderDetail(orderId)) },
            )
        }
        composable(
            route = AppRoutes.AvailableOrderDetail,
            arguments = listOf(navArgument(AppRoutes.OrderIdArg) { type = NavType.StringType }),
        ) {
            AvailableOrderDetailScreen(
                onBack = { navController.popBackStack() },
                onUnavailable = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(AppRoutes.OrderUnavailableResult, true)
                    navController.popBackStack()
                },
                onCompleteProfile = { navController.navigate(AppRoutes.CompleteProfile) },
            )
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
