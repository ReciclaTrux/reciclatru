import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/design_system/theme/rt_theme.dart';
import 'health_check_screen.dart';

/// Entrada de diagnóstico, aparte de la app:
/// `flutter run -t lib/dev/health_check_main.dart --dart-define=API_BASE_URL=...`
void main() {
  runApp(const ProviderScope(child: _HealthCheckApp()));
}

class _HealthCheckApp extends StatelessWidget {
  const _HealthCheckApp();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'ReciclaTru · Prueba de conexión',
      theme: rtTheme,
      home: const HealthCheckScreen(),
    );
  }
}
