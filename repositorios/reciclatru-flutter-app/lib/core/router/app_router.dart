
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../network/network_providers.dart';
import '../session/session_store.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/profile/presentation/complete_profile_screen.dart';
import '../../features/profile/presentation/profile_screen.dart';
import '../../features/profile/presentation/location_picker_screen.dart';
import '../../features/profile/presentation/location_confirmation_screen.dart';
import '../../features/profile/domain/user_profile.dart';
import '../../main.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final sessionStore = ref.watch(sessionStoreProvider);

  final router = GoRouter(
    initialLocation: '/login',
    refreshListenable: sessionStore,
    redirect: (context, state) {
      final session = sessionStore.currentSession;
      final needsProfile = sessionStore.needsProfile == true;
      final path = state.uri.path;

      final isAuthRoute = path == '/login' || path == '/register';
      final isPickupRoute =
          path == '/pickup-location' || path == '/pickup-location/confirm';

      // Sin sesión solo puede estar en login o registro.
      if (session == null) {
        if (!isAuthRoute) return '/login';
        return null;
      }

      // La ubicación solo está disponible para vecinos.
      if (isPickupRoute && session.role != 'NEIGHBOR') {
        return needsProfile ? '/complete-profile' : '/home';
      }

      // P07 (confirmación de ubicación) necesita recibir las coordenadas desde P06.
      if (path == '/pickup-location/confirm' && state.extra is! PickupAddress) {
        return '/pickup-location';
      }

      // HU-03: si falta completar perfil, no puede ir a home ni perfil.
      if (needsProfile) {
        if (path != '/complete-profile' && !isPickupRoute) {
          return '/complete-profile';
        }
        return null;
      }

      // Usuario autenticado con perfil completo que intenta entrar a auth o complete-profile.
      if (isAuthRoute || path == '/' || path == '/complete-profile') {
        return '/home';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/login',
        builder: (context, state) {
          final expired = sessionStore.lastSignOut == SignOutReason.expired;
          return LoginScreen(sessionExpired: expired);
        },
      ),
      GoRoute(
        path: '/register',
        builder: (context, state) => const RegisterScreen(),
      ),
      GoRoute(
        path: '/complete-profile',
        builder: (context, state) => CompleteProfileScreen(
          onCompleted: () {
            sessionStore.markProfileCompleted();
            context.go('/home');
          },
          onChoosePickupPoint: () async {
            await context.push<bool>('/pickup-location');
          },
        ),
      ),
      GoRoute(
        path: '/pickup-location',
        builder: (context, state) => LocationPickerScreen(
          initialPoint: state.extra is PickupAddress
              ? state.extra as PickupAddress
              : null,
        ),
      ),
      GoRoute(
        path: '/pickup-location/confirm',
        builder: (context, state) => LocationConfirmationScreen(
          point: state.extra as PickupAddress,
        ),
      ),
      GoRoute(
        path: '/home',
        builder: (context, state) => const HomePlaceholder(),
      ),
      GoRoute(
        path: '/profile',
        builder: (context, state) => const ProfileScreen(),
      ),
    ],
  );

  ref.onDispose(router.dispose);
  return router;
});
