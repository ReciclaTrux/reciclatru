import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum SignOutReason { logout, expired }

class AppSession {
  const AppSession({required this.userId, required this.token, required this.role});
  final String userId;
  final String token;
  final String role;
}

/// Equivalente funcional del SessionStore Android (DataStore): persiste
/// userId/token/role. En Flutter usa SharedPreferences, según HU-02.
class SessionStore extends ChangeNotifier {
  static const _idKey = 'session_user_id';
  static const _tokenKey = 'session_token';
  static const _roleKey = 'session_role';

  AppSession? _session;
  AppSession? get currentSession => _session;
  bool? _needsProfile;
  bool? get needsProfile => _needsProfile;
  SignOutReason? _lastSignOut;
  SignOutReason? get lastSignOut => _lastSignOut;

  Future<AppSession?> loadSession() async {
    final prefs = await SharedPreferences.getInstance();
    final id = prefs.getString(_idKey);
    final token = prefs.getString(_tokenKey);
    final role = prefs.getString(_roleKey);
    _session = id != null && id.isNotEmpty && token != null && token.isNotEmpty &&
            (role == 'NEIGHBOR' || role == 'RECYCLER')
        ? AppSession(userId: id, token: token, role: role!)
        : null;
    _needsProfile = null; // Al reiniciar, Kotlin envía a Home.
    notifyListeners();
    return _session;
  }

  Future<void> save(AppSession session, {bool needsProfile = false}) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_idKey, session.userId);
    await prefs.setString(_tokenKey, session.token);
    await prefs.setString(_roleKey, session.role);
    _session = session;
    _needsProfile = needsProfile;
    _lastSignOut = null;
    notifyListeners();
  }

  Future<void> clear([SignOutReason reason = SignOutReason.logout]) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_idKey);
    await prefs.remove(_tokenKey);
    await prefs.remove(_roleKey);
    _session = null;
    _needsProfile = null;
    _lastSignOut = reason;
    notifyListeners();
  }
}
