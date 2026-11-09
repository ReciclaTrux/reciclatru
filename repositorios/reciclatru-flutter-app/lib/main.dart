import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:maplibre_gl/maplibre_gl.dart';
import 'core/design_system/atoms/rt_button.dart';
import 'core/design_system/theme/rt_spacing.dart';
import 'core/design_system/theme/rt_theme.dart';
import 'core/network/network_providers.dart';
import 'core/session/session_store.dart';
import 'features/auth/presentation/auth_provider.dart';
import 'features/auth/presentation/login_screen.dart';
import 'features/auth/presentation/register_screen.dart';

void main() {
  MapLibreMap.useHybridComposition = true;
  runApp(const ProviderScope(child: ReciclaTruApp()));
}

class ReciclaTruApp extends ConsumerWidget {
  const ReciclaTruApp({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => MaterialApp(
    title: 'ReciclaTru',
    debugShowCheckedModeBanner: false,
    theme: rtTheme,
    home: const SessionGate(),
    routes: {
      '/login': (_) => const LoginScreen(),
      '/register': (_) => const RegisterScreen(),
    },
  );
}

/// Como AppSessionViewModel/AppNavHost.kt: el arranque depende de la sesión.
class SessionGate extends ConsumerStatefulWidget {
  const SessionGate({super.key});
  @override
  ConsumerState<SessionGate> createState() => _SessionGateState();
}

class _SessionGateState extends ConsumerState<SessionGate> {
  bool _loaded = false;
  AppSession? _session;
  SignOutReason? _reason;
  late final SessionStore _store;

  @override
  void initState() {
    super.initState();
    _store = ref.read(sessionStoreProvider);
    _store.addListener(_changed);
    _store.loadSession().then((_) {
      if (mounted) setState(() => _loaded = true);
    });
  }

  void _changed() {
    if (!mounted) return;
    setState(() {
      _session = _store.currentSession;
      _reason = _store.lastSignOut;
    });
  }

  @override
  void dispose() {
    _store.removeListener(_changed);
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_loaded) {
      return const Scaffold(body: SizedBox.expand());
    }
    if (_session == null) {
      return LoginScreen(key: ValueKey('login_$_reason'),
        sessionExpired: _reason == SignOutReason.expired);
    }
    if (_store.needsProfile == true) {
      return const CompleteProfilePlaceholder();
    }
    return HomePlaceholder(key: ValueKey(_session!.userId));
  }
}

/// El proyecto Flutter recibido todavía no tiene la pantalla de perfil de
/// Kotlin (HU-03). Se deja un destino provisional, no se atribuye equivalencia
/// visual a este placeholder.
class HomePlaceholder extends ConsumerWidget {
  const HomePlaceholder({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    body: SafeArea(child: Center(child: Padding(
      padding: const EdgeInsets.all(RtSpacing.s4),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        Text('ReciclaTru', style: Theme.of(context).textTheme.headlineMedium),
        const SizedBox(height: RtSpacing.s4),
        const Text('Conectamos a los vecinos de Trujillo con recicladores.'),
        const SizedBox(height: RtSpacing.s4),
        const Text('Esta es la base de la app. Los pedidos llegan en el Sprint 2.'),
        const SizedBox(height: RtSpacing.s4),
        RtButton(text: 'Mi perfil', fullWidth: true,
          onClick: () => Navigator.push(context, MaterialPageRoute<void>(
            builder: (_) => const ProfilePlaceholder()))),
      ]),
    ))),
  );
}

class ProfilePlaceholder extends ConsumerWidget {
  const ProfilePlaceholder({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(title: const Text('Mi perfil')),
    body: SafeArea(child: Padding(
      padding: const EdgeInsets.all(RtSpacing.s4),
      child: RtButton(text: 'Cerrar sesión', fullWidth: true,
        onClick: () async {
          await ref.read(authRepositoryProvider).logout();
          if (context.mounted) {
            Navigator.of(context).popUntil((route) => route.isFirst);
          }
        }),
    )),
  );
}

/// Sustituir por la pantalla HU-03 al integrarse, para mantener navegación Kotlin.
class CompleteProfilePlaceholder extends ConsumerWidget {
  const CompleteProfilePlaceholder({super.key});
  @override
  Widget build(BuildContext context, WidgetRef ref) => Scaffold(
    appBar: AppBar(title: const Text('Completar perfil')),
    body: SafeArea(child: Center(child: Padding(
      padding: const EdgeInsets.all(RtSpacing.s4),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        const Text('Pantalla de completar perfil pendiente de migración HU-03.'),
        const SizedBox(height: RtSpacing.s4),
        RtButton(text: 'Cerrar sesión', onClick: () async {
          await ref.read(authRepositoryProvider).logout();
        }),
      ]),
    ))),
  );
}
