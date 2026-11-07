import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/network/network_providers.dart'; // Importa el dioProvider global
import '../data/auth_repository.dart';
import '../domain/user_model.dart';

// Proveedor del repositorio conectado al Dio global
final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return AuthRepository(dio); // ✅ Ahora sí le pasamos el cliente Dio correctamente
});

// Notificador moderno para el usuario autenticado
final authUserProvider = NotifierProvider<AuthUserNotifier, UserModel?>(() {
  return AuthUserNotifier();
});

class AuthUserNotifier extends Notifier<UserModel?> {
  @override
  UserModel? build() => null;

  set user(UserModel? newUser) => state = newUser;
}

// Notificador moderno para el estado de carga
final authLoadingProvider = NotifierProvider<AuthLoadingNotifier, bool>(() {
  return AuthLoadingNotifier();
});

class AuthLoadingNotifier extends Notifier<bool> {
  @override
  bool build() => false;

  set isLoading(bool value) => state = value;
}