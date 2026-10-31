import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import 'core/design_system/atoms/rt_spinner.dart';
import 'core/design_system/molecules/rt_inline_alert.dart';
import 'core/design_system/theme/rt_colors.dart';
import 'core/design_system/theme/rt_spacing.dart';
import 'core/design_system/theme/rt_theme.dart';
import 'core/network/api_exception.dart';
import 'core/network/backend_status_notifier.dart';

void main() {
  // La vista de Android por defecto usa GLSurfaceView (Virtual Display), que
  // queda por encima de los overlays de Flutter y se queda con los toques.
  // TextureView deja que las sugerencias, el pin y la hoja reciban los taps.
  MapLibreMap.useHybridComposition = true;
  runApp(const ProviderScope(child: ReciclaTruApp()));
}

/// Punto de entrada de la app. El tema del sistema se aplica aquí.
class ReciclaTruApp extends StatelessWidget {
  const ReciclaTruApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ReciclaTru',
      theme: rtTheme,
      home: const _StartupPlaceholder(),
    );
  }
}

/// Marcador de posición mientras las HU agregan sus pantallas.
///
/// Sigue la indicación del sistema para el nombre: "ReciclaTru" en
/// `headline-lg` y color `ods-12`.
class _StartupPlaceholder extends ConsumerWidget {
  const _StartupPlaceholder();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final extra = theme.extension<RtExtraColors>()!;
    final status = ref.watch(backendStatusProvider);

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: Padding(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: <Widget>[
                Text(
                  'ReciclaTru',
                  textAlign: TextAlign.center,
                  style: theme.textTheme.headlineMedium?.copyWith(
                    color: extra.ods12,
                  ),
                ),
                const SizedBox(height: RtSpacing.s4),
                status.when(
                  loading: () => const Center(
                    child: RtSpinner(key: Key('spinner_conexion_backend')),
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
                        : 'No pudimos conectar con la API. Intenta de nuevo.',
                    actionLabel: 'Reintentar',
                    onAction: ref.read(backendStatusProvider.notifier).retry,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
