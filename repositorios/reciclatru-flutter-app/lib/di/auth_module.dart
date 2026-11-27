/// Registra las dependencias de auth module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/repository/auth/auth_repository_impl.dart' as implementation;
import '../di/network_module.dart';
import '../domain/repository/auth/auth_repository.dart';
import '../domain/usecase/auth/login_use_case.dart';
import '../domain/usecase/auth/logout_use_case.dart';
import '../domain/usecase/auth/register_use_case.dart';
import 'session_module.dart';

/// Provee la implementación de autenticación para las pantallas y casos de uso.
final authRepositoryProvider = Provider<AuthRepository>((ref) =>
    implementation.AuthRepositoryImpl(ref.watch(dioProvider), ref.watch(sessionStoreProvider)));

/// Casos de uso que consume la capa de presentación.
final loginUseCaseProvider = Provider<LoginUseCase>((ref) =>
    LoginUseCase(ref.watch(authRepositoryProvider)));
final registerUseCaseProvider = Provider<RegisterUseCase>((ref) =>
    RegisterUseCase(ref.watch(authRepositoryProvider)));
final logoutUseCaseProvider = Provider<LogoutUseCase>((ref) =>
    LogoutUseCase(ref.watch(authRepositoryProvider)));
