/// Coordina las acciones y el estado de app session view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../data/local/session/session_store.dart';
import '../../di/session_module.dart';
import '../../model/session/sign_out_reason.dart';
import 'app_session_ui_state.dart';

/// Proveedor para el estado de autenticación inicial de la aplicación.
final appSessionViewModelProvider =
    NotifierProvider<AppSessionViewModel, AppSessionUiState>(
  AppSessionViewModel.new,
);

/// Inicializa la sesión y decide el primer destino, como SessionViewModel.kt.
class AppSessionViewModel extends Notifier<AppSessionUiState> {
  late SessionStore _sessionStore;

  @override
  AppSessionUiState build() {
    _sessionStore = ref.read(sessionStoreProvider);
    _sessionStore.addListener(_onStoreChanged);
    ref.onDispose(() => _sessionStore.removeListener(_onStoreChanged));
    return const AppSessionUiState();
  }

  /// Lee el almacenamiento seguro antes de mostrar la navegación.
  Future<void> initialize() async {
    final session = await _sessionStore.loadSession();
    state = AppSessionUiState(
      session: session,
      startDestination: session == null ? '/login' : '/home',
      lastSignOut: _sessionStore.lastSignOut,
      initialized: true,
    );
  }

  /// Notifica la ruta de ingreso después de cerrar sesión o vencer el token.
  void onSignedOut(SignOutReason reason) {
    state = AppSessionUiState(
      startDestination: '/login',
      lastSignOut: reason,
      initialized: true,
    );
  }

  /// Refleja cambios producidos por ingreso, cierre o expiración de sesión.
  void _onStoreChanged() {
    final session = _sessionStore.currentSession;
    final signOut = _sessionStore.lastSignOut;
    state = AppSessionUiState(
      session: session,
      startDestination: session == null ? '/login' : '/home',
      lastSignOut: signOut,
      initialized: true,
    );
  }
}
