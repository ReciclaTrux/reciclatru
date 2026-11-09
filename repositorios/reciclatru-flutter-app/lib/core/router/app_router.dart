import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../network/network_providers.dart';
import '../session/session_store.dart';
import '../../features/auth/presentation/login_screen.dart';
import '../../features/auth/presentation/register_screen.dart';
import '../../features/profile/presentation/complete_profile_screen.dart';
import '../../main.dart';

final routerProvider = Provider<GoRouter>((ref) {
  final sessionStore = ref.watch(sessionStoreProvider);

  return GoRouter(
    initialLocation: '/',
    refreshListenable: sessionStore,
    redirect: (context, state) {
      final session = sessionStore.currentSession;
      final needsProfile = sessionStore.needsProfile == true;
      final path = state.uri.path;
      final isAuthOrWelcome = path == '/' || path == '/login' || path == '/register';

      if (session == null) {
        if (!isAuthOrWelcome) return '/';
        return null;
      }

      if (needsProfile) {
        if (path != '/complete-profile') return '/complete-profile';
        return null;
      }

      if (isAuthOrWelcome || path == '/complete-profile') {
        return '/home';
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/',
        builder: (context, state) => const WelcomeScreen(),
      ),
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
          },
          onChoosePickupPoint: () async {
            // HU-04 map picker placeholder
          },
        ),
      ),
      GoRoute(
        path: '/home',
        builder: (context, state) => const HomePlaceholder(),
      ),
      GoRoute(
        path: '/profile',
        builder: (context, state) => const ProfilePlaceholder(),
      ),
    ],
  );
});
