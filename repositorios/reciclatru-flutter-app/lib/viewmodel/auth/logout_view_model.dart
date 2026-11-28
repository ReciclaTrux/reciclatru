/// Coordina las acciones y el estado de logout view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../di/auth_module.dart';

/// Ejecuta el cierre local de sesión definido por `LogoutViewModel.kt`.
final logoutViewModelProvider = Provider<LogoutViewModel>((ref) => LogoutViewModel(ref));

class LogoutViewModel {
  const LogoutViewModel(this._ref);
  final Ref _ref;

  /// Borra token e identidad del almacenamiento seguro.
  Future<void> onLogout() => _ref.read(logoutUseCaseProvider).call();
}
