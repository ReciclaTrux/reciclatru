import '../../../core/session/session_store.dart';

class LoginResult {
  const LoginResult({required this.session, required this.profileComplete});
  final AppSession session;
  final bool profileComplete;
}
