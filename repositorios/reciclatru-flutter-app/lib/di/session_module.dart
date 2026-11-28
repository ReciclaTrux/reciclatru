/// Registra las dependencias de session module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/local/session/session_store.dart';

/// Proporciona la única instancia local de sesión para toda la aplicación.
final sessionStoreProvider = Provider<SessionStore>((ref) {
  final store = SessionStore();
  ref.onDispose(store.dispose);
  return store;
});
