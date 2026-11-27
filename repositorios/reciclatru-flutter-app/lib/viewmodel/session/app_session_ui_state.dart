/// Define los datos de interfaz de app session y cómo se actualiza su estado.
import '../../model/session/session.dart';
import '../../model/session/sign_out_reason.dart';

/// Estado de arranque y sesión que consume la navegación.
class AppSessionUiState {
  const AppSessionUiState({
    this.startDestination,
    this.session,
    this.lastSignOut,
    this.initialized = false,
  });

  final String? startDestination;
  final Session? session;
  final SignOutReason? lastSignOut;
  final bool initialized;

  /// Rol activo, igual que la propiedad `role` del ViewModel Kotlin.
  UserRole? get role => session?.role;
}
