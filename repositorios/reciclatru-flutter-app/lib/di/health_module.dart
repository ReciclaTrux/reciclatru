/// Registra las dependencias de health module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/remote/api/health/health_api.dart';
import '../data/repository/health/health_repository_impl.dart';
import '../di/network_module.dart';
import '../domain/repository/health/health_repository.dart';

/// Registra la API y el repositorio de salud en el contenedor de dependencias.
final healthApiProvider = Provider<HealthApi>((ref) => HealthApi(ref.watch(dioProvider)));
final healthRepositoryProvider = Provider<HealthRepository>((ref) =>
    HealthRepositoryImpl(ref.watch(healthApiProvider)));
