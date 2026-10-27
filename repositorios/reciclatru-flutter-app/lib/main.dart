import 'package:flutter/material.dart';

import 'core/design_system/theme/rt_colors.dart';
import 'core/design_system/theme/rt_theme.dart';

void main() {
  runApp(const ReciclaTruApp());
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
class _StartupPlaceholder extends StatelessWidget {
  const _StartupPlaceholder();

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final extra = theme.extension<RtExtraColors>()!;

    return Scaffold(
      body: Center(
        child: Text(
          'ReciclaTru',
          style: theme.textTheme.headlineMedium?.copyWith(color: extra.ods12),
        ),
      ),
    );
  }
}
