/// Punto de entrada de Flutter: prepara dependencias, carga la sesión persistida y abre la aplicación.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_theme.dart';
import 'package:reciclatru/viewmodel/session/app_session_view_model.dart';
import 'package:reciclatru/view/navigation/app_nav_host.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  MapLibreMap.useHybridComposition = true;

  final container = ProviderContainer();
  // La carga de sesión y el destino inicial viven en el ViewModel de sesión.
  await container.read(appSessionViewModelProvider.notifier).initialize();

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
