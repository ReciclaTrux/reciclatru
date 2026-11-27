/// Implementa el repositorio health repository impl conectando los contratos del dominio con sus fuentes de datos.
import '../../../data/remote/api/health/health_api.dart';
import '../../../domain/repository/health/health_repository.dart';

/// Implementación del chequeo de salud usando el endpoint remoto.
class HealthRepositoryImpl implements HealthRepository {
  const HealthRepositoryImpl(this._api);
  final HealthApi _api;
  @override
  Future<HealthStatus> check() => _api.check();
}
