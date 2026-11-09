import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/network_providers.dart';
import '../data/auth_repository.dart';
import '../domain/user_model.dart';

// Una sola definición del provider (sin duplicarla en auth_repository.dart).
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(ref.watch(dioProvider), ref.watch(sessionStoreProvider));
});

// Se conserva por compatibilidad con otras pantallas del proyecto.
final authUserProvider = NotifierProvider<AuthUserNotifier, UserModel?>(AuthUserNotifier.new);
class AuthUserNotifier extends Notifier<UserModel?> {
  @override
  UserModel? build() => null;
  set user(UserModel? value) => state = value;
}

final authLoadingProvider = NotifierProvider<AuthLoadingNotifier, bool>(AuthLoadingNotifier.new);
class AuthLoadingNotifier extends Notifier<bool> {
  @override
  bool build() => false;
  set isLoading(bool value) => state = value;
}
