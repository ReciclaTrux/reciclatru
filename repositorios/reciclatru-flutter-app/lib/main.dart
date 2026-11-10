import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import 'core/design_system/atoms/rt_button.dart';
import 'core/design_system/atoms/rt_spinner.dart';
import 'core/design_system/molecules/rt_inline_alert.dart';
import 'core/design_system/theme/rt_spacing.dart';
import 'core/design_system/theme/rt_theme.dart';
import 'core/network/api_exception.dart';
import 'core/network/backend_status_notifier.dart';
import 'core/network/network_providers.dart';
import 'core/router/app_router.dart';

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

// ==========================================================
// PANTALLA PRINCIPAL (HomePlaceholder de Android AppNavHost.kt)
// ==========================================================

class HomePlaceholder extends ConsumerWidget {
  const HomePlaceholder({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final sessionStore = ref.watch(sessionStoreProvider);
    final isNeighbor = sessionStore.currentSession?.role == 'NEIGHBOR';
    final status = ref.watch(backendStatusProvider);
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                Text(
                  'ReciclaTru',
                  style: theme.textTheme.headlineMedium?.copyWith(
                    color: theme.colorScheme.primary,
                  ),
                ),
                const SizedBox(height: RtSpacing.s2),
                Text(
                  'Conectamos a los vecinos de Trujillo con recicladores.',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.bodyLarge?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(height: RtSpacing.s4),
                // Backend Health Status
                status.when(
                  loading: () => const Center(
                    child: RtSpinner(
                      key: Key('spinner_conexion_backend'),
                    ),
                  ),
                  data: (_) => const RtInlineAlert(
                    key: Key('alert_conexion_backend'),
                    tone: RtInlineAlertTone.success,
                    message: 'Conectado',
                  ),
                  error: (error, _) => RtInlineAlert(
                    key: const Key('alert_conexion_backend'),
                    tone: RtInlineAlertTone.error,
                    message: error is ApiException
                        ? error.message
                        : 'No pudimos conectar con el servidor.',
                    actionLabel: 'Reintentar',
                    onAction: ref.read(backendStatusProvider.notifier).retry,
                  ),
                ),
                const SizedBox(height: RtSpacing.s4),
                const RtInlineAlert(
                  tone: RtInlineAlertTone.info,
                  message:
                      'Esta es la base de la app. Los pedidos llegan en el Sprint 2.',
                ),
                const SizedBox(height: RtSpacing.s4),
                if (isNeighbor) ...[
                  RtButton(
                    key: const Key('btn_abrir_ubicacion'),
                    text: 'Marcar punto de recojo',
                    fullWidth: true,
                    onClick: () {
                      context.push('/pickup-location');
                    },
                  ),
                  const SizedBox(height: RtSpacing.s4),
                ],
                RtButton(
                  key: const Key('btn_abrir_perfil'),
                  text: 'Mi perfil',
                  fullWidth: true,
                  variant: RtButtonVariant.secondary,
                  onClick: () {
                    context.push('/profile');
                  },
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
