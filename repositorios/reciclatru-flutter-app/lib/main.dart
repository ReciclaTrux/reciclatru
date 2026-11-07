
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import 'core/design_system/atoms/rt_button.dart';
import 'core/design_system/atoms/rt_spinner.dart';
import 'core/design_system/molecules/rt_inline_alert.dart';
import 'core/design_system/theme/rt_colors.dart';
import 'core/design_system/theme/rt_spacing.dart';
import 'core/design_system/theme/rt_theme.dart';
import 'core/network/api_exception.dart';
import 'core/network/backend_status_notifier.dart';

import 'features/auth/presentation/register_screen.dart';
import 'features/auth/presentation/login_screen.dart';

void main() {
  MapLibreMap.useHybridComposition = true;
  runApp(const ProviderScope(child: ReciclaTruApp()));
}

class ReciclaTruApp extends StatelessWidget {
  const ReciclaTruApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ReciclaTru',
      debugShowCheckedModeBanner: false,
      theme: rtTheme,
      home: const WelcomeScreen(),
      routes: {
        '/register': (context) => const RegisterScreen(),
        '/login': (context) => const LoginScreen(),
      },
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
                    Navigator.pushNamed(context, '/login');
                  },
                ),

                const SizedBox(height: 16),

                RtButton(
                  text: 'Registrarme',
                  fullWidth: true,
                  onClick: () {
                    Navigator.pushNamed(context, '/register');
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
