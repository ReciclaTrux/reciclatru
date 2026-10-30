import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/design_system/atoms/rt_button.dart';
import '../core/design_system/atoms/rt_spinner.dart';
import '../core/design_system/molecules/rt_error_state.dart';
import '../core/design_system/molecules/rt_inline_alert.dart';
import '../core/design_system/theme/rt_spacing.dart';
import '../core/network/api_config.dart';
import '../core/network/api_exception.dart';
import 'health_check_notifier.dart';

/// Pantalla de diagnóstico: prueba la conexión con el endpoint de salud.
class HealthCheckScreen extends ConsumerWidget {
  const HealthCheckScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final state = ref.watch(healthCheckProvider);
    final notifier = ref.read(healthCheckProvider.notifier);

    final Widget content;
    if (state.isLoading) {
      content = const Padding(
        padding: EdgeInsets.symmetric(vertical: RtSpacing.s6),
        child: RtSpinner(size: RtSpinnerSize.large),
      );
    } else if (state.hasError) {
      final error = state.error;
      final isServerError =
          error is ApiException && error.kind == ApiFailureKind.serverError;
      content = RtErrorState(
        title: isServerError ? 'La API tuvo un problema' : 'Sin conexión',
        message: error is ApiException
            ? error.message
            : 'No pudimos completar la prueba. Intenta de nuevo.',
        kind: isServerError
            ? RtErrorKind.serverError
            : RtErrorKind.noConnection,
        onRetry: notifier.check,
      );
    } else if (state.value != null) {
      final health = state.value!;
      content = Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          RtInlineAlert(
            tone: RtInlineAlertTone.success,
            title: 'La API responde',
            message:
                'Estado: ${health.status ?? 'sin dato'} · '
                'HTTP ${health.statusCode} · '
                '${health.elapsed.inMilliseconds} ms',
          ),
          const SizedBox(height: RtSpacing.s4),
          RtButton(
            key: const Key('btn_probar_de_nuevo'),
            text: 'Probar de nuevo',
            variant: RtButtonVariant.secondary,
            fullWidth: true,
            onClick: notifier.check,
          ),
        ],
      );
    } else {
      content = RtButton(
        key: const Key('btn_probar_conexion'),
        text: 'Probar conexión',
        fullWidth: true,
        onClick: notifier.check,
      );
    }

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: Padding(
              padding: const EdgeInsets.all(RtSpacing.s5),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Text(
                    'Prueba de conexión',
                    style: theme.textTheme.headlineMedium,
                  ),
                  const SizedBox(height: RtSpacing.s2),
                  Text(
                    '$apiBaseUrl$apiHealthPath',
                    style: theme.textTheme.bodyLarge!.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: RtSpacing.s5),
                  content,
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
