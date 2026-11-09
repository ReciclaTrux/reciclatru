import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import 'core/design_system/atoms/rt_button.dart';
import 'core/design_system/atoms/rt_spinner.dart';
import 'core/design_system/molecules/rt_inline_alert.dart';
import 'core/design_system/theme/rt_colors.dart';
import 'core/design_system/theme/rt_spacing.dart';
import 'core/design_system/theme/rt_theme.dart';
import 'core/network/api_exception.dart';
import 'core/network/backend_status_notifier.dart';
import 'core/network/network_providers.dart';
import 'core/router/app_router.dart';
import 'core/session/session_store.dart';
import 'features/auth/presentation/auth_provider.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  MapLibreMap.useHybridComposition = true;

  final container = ProviderContainer();
  await container.read(sessionStoreProvider).loadSession();

  runApp(
    UncontrolledProviderScope(
      container: container,
      child: const ReciclaTruApp(),
    ),
  );
}

class ReciclaTruApp extends ConsumerWidget {
  const ReciclaTruApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);

    return MaterialApp.router(
      title: 'ReciclaTru',
      debugShowCheckedModeBanner: false,
      theme: rtTheme,
      routerConfig: router,
    );
  }
}

class WelcomeScreen extends ConsumerWidget {
  const WelcomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final extra = theme.extension<RtExtraColors>()!;
    final status = ref.watch(backendStatusProvider);

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const SizedBox(height: 30),
                Icon(
                  Icons.recycling,
                  size: 90,
                  color: extra.ods12,
                ),
                const SizedBox(height: 20),
                Text(
                  'ReciclaTru',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.headlineMedium?.copyWith(
                    color: extra.ods12,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                const SizedBox(height: 12),
                Text(
                  'Dale una segunda oportunidad a los materiales '
                  'y contribuye a un futuro más sostenible.',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.bodyLarge,
                ),
                const SizedBox(height: 35),
                RtButton(
                  text: 'Iniciar sesión',
                  fullWidth: true,
                  onClick: () {
                    context.go('/login');
                  },
                ),
                const SizedBox(height: 16),
                RtButton(
                  text: 'Registrarme',
                  fullWidth: true,
                  onClick: () {
                    context.go('/register');
                  },
                ),
                const SizedBox(height: 35),
                const Divider(),
                const SizedBox(height: 12),
                Text(
                  'Estado del servidor',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.titleSmall,
                ),
                const SizedBox(height: 12),
                status.when(
                  loading: () => const Center(
                    child: RtSpinner(
                      key: Key('spinner_conexion_backend'),
                    ),
                  ),
                  data: (_) => const RtInlineAlert(
                    key: Key('alert_conexion_backend'),
                    tone: RtInlineAlertTone.success,
                    message: 'Servidor conectado',
                  ),
                  error: (error, _) => RtInlineAlert(
                    key: const Key('alert_conexion_backend'),
                    tone: RtInlineAlertTone.error,
                    message: error is ApiException
                        ? error.message
                        : 'No pudimos conectar con el servidor.',
                    actionLabel: 'Reintentar',
                    onAction: ref
                        .read(backendStatusProvider.notifier)
                        .retry,
                  ),
                ),
                const SizedBox(height: 20),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class HomePlaceholder extends ConsumerWidget {
  const HomePlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
        body: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(RtSpacing.s4),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(
                    'ReciclaTru',
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                  const SizedBox(height: RtSpacing.s4),
                  const Text(
                    'Conectamos a los vecinos de Trujillo con recicladores.',
                  ),
                  const SizedBox(height: RtSpacing.s4),
                  const Text(
                    'Esta es la base de la app. Los pedidos llegan en el Sprint 2.',
                  ),
                  const SizedBox(height: RtSpacing.s4),
                  RtButton(
                    text: 'Mi perfil',
                    fullWidth: true,
                    onClick: () => context.go('/profile'),
                  ),
                ],
              ),
            ),
          ),
        ),
      );
}

class ProfilePlaceholder extends ConsumerWidget {
  const ProfilePlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
        appBar: AppBar(title: const Text('Mi perfil')),
        body: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: RtButton(
              text: 'Cerrar sesión',
              fullWidth: true,
              onClick: () async {
                await ref.read(authRepositoryProvider).logout();
                // go_router redirect automáticamente redirige a / o /login
              },
            ),
          ),
        ),
      );
}
