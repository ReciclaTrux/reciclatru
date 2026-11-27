/// Implementa el acceso local de session store para persistencia o servicios del dispositivo.
import 'package:flutter/foundation.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:reciclatru/model/session/session.dart';
import 'package:reciclatru/model/session/sign_out_reason.dart';

/// Conserva userId, token y rol de la sesión para restaurarla al abrir la app.
/// El token se escribe en el almacenamiento seguro del dispositivo.
class SessionStore extends ChangeNotifier {
  static const _idKey = 'session_user_id';
  static const _tokenKey = 'session_token';
  static const _roleKey = 'session_role';

  /// Capa cifrada respaldada por el sistema operativo en cada plataforma.
  final FlutterSecureStorage _storage;

  /// Permite sustituir el almacenamiento por uno falso en pruebas.
  SessionStore({FlutterSecureStorage storage = const FlutterSecureStorage()})
      : _storage = storage;

  Session? _session;
  Session? get currentSession => _session;
  bool? _needsProfile;
  bool? get needsProfile => _needsProfile;
  SignOutReason? _lastSignOut;
  SignOutReason? get lastSignOut => _lastSignOut;

  Future<Session?> loadSession() async {
    // Se recuperan los tres valores que conforman la sesión de Kotlin.
    final id = await _storage.read(key: _idKey);
    final token = await _storage.read(key: _tokenKey);
    final role = await _storage.read(key: _roleKey);
    final parsedRole = UserRole.fromApiValue(role);
    _session = id != null && id.isNotEmpty && token != null && token.isNotEmpty && parsedRole != null
        ? Session(userId: id, token: token, role: parsedRole)
        : null;
    _needsProfile = null; // Al reiniciar, Kotlin envía a Home.
    notifyListeners();
    return _session;
  }

  Future<void> save(Session session, {bool needsProfile = false}) async {
    // La sesión se persiste en almacenamiento cifrado, incluyendo el token.
    await _storage.write(key: _idKey, value: session.userId);
    await _storage.write(key: _tokenKey, value: session.token);
    await _storage.write(key: _roleKey, value: session.role.apiValue);
    _session = session;
    _needsProfile = needsProfile;
    _lastSignOut = null;
    notifyListeners();
  }

  Future<void> clear([SignOutReason reason = SignOutReason.logout]) async {
    // El cierre de sesión elimina las mismas claves que se guardaron al ingresar.
    await _storage.delete(key: _idKey);
    await _storage.delete(key: _tokenKey);
    await _storage.delete(key: _roleKey);
    _session = null;
    _needsProfile = null;
    _lastSignOut = reason;
    notifyListeners();
  }

  /// Actualiza el estado local después de completar el perfil.
  /// Notifica a SessionGate para continuar con la navegación.
  void markProfileCompleted() {
    _needsProfile = false;
    notifyListeners();
  }
}
