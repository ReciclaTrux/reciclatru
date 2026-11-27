/// Construye la pantalla o navegación de app nav host conservando el flujo de usuario de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../data/local/session/session_store.dart';
import '../../model/session/sign_out_reason.dart';
import '../../model/session/session.dart';
import '../../di/network_module.dart';
import '../../di/session_module.dart';
import '../../viewmodel/session/app_session_view_model.dart';
import '../auth/login_screen.dart';
import '../auth/register_screen.dart';
import '../explore/available_order_detail_screen.dart';
import '../explore/available_orders_screen.dart';
import '../orders/create_order_screen.dart';
import '../orders/edit_order_screen.dart';
import '../orders/my_orders_screen.dart';
import '../orders/order_detail_screen.dart';
import '../profile/complete_profile_screen.dart';
import '../profile/location_screen.dart';
import '../profile/profile_screen.dart';

/// Rutas de navegación traducidas de AppRoutes y AppNavHost.kt.
final routerProvider = Provider<GoRouter>((ref) {
  final sessionStore = ref.watch(sessionStoreProvider);
  final sessionState = ref.read(appSessionViewModelProvider);
  final router = GoRouter(
    initialLocation: sessionState.startDestination ?? '/login',
    refreshListenable: sessionStore,
    redirect: (context, state) {
      final session = sessionStore.currentSession;
      final path = state.uri.path;
      final authRoute = path == '/login' || path == '/register';
      final pickupRoute = path == '/pickup-location';
      if (session == null) return authRoute ? null : '/login';
      if (pickupRoute && session.role != UserRole.neighbor) return sessionStore.needsProfile == true ? '/complete-profile' : '/home';
      if (sessionStore.needsProfile == true) {
        if (path != '/complete-profile' && !pickupRoute) return '/complete-profile';
        return null;
      }
      if (authRoute || path == '/') return '/home';
      return null;
    },
    routes: [
      GoRoute(path: '/login', builder: (context, state) => LoginScreen(
        sessionExpired: (sessionStore.lastSignOut ?? sessionState.lastSignOut) == SignOutReason.expired,
      )),
      GoRoute(path: '/register', builder: (context, state) => const RegisterScreen()),
      GoRoute(path: '/complete-profile', builder: (context, state) => CompleteProfileScreen(
        onCompleted: () { sessionStore.markProfileCompleted(); context.go('/home'); },
        onChoosePickupPoint: () async { await context.push('/pickup-location'); },
      )),
      GoRoute(path: '/pickup-location', builder: (context, state) => const LocationScreen()),
      GoRoute(path: '/home', builder: (context, state) =>
        sessionStore.currentSession?.role == UserRole.neighbor
            ? const MyOrdersScreen()
            : const AvailableOrdersScreen()),
      GoRoute(path: '/profile', builder: (context, state) => const ProfileScreen()),
      GoRoute(path: '/orders/create', builder: (context, state) => const CreateOrderScreen()),
      GoRoute(path: '/orders/:orderId', builder: (context, state) =>
        OrderDetailScreen(orderId: state.pathParameters['orderId']!)),
      GoRoute(path: '/orders/:orderId/edit', builder: (context, state) =>
        EditOrderScreen(orderId: state.pathParameters['orderId']!)),
      GoRoute(path: '/explore/:orderId', builder: (context, state) =>
        AvailableOrderDetailScreen(
          orderId: state.pathParameters['orderId']!,
          onUnavailable: () => context.pop(true),
          onCompleteProfile: () => context.push('/complete-profile'),
        )),
    ],
  );
  ref.onDispose(router.dispose);
  return router;
});
