// Este caso de uso comprueba si los servicios del servidor están funcionando correctamente.
import '../../repository/health/health_repository.dart';
import '../../../data/remote/api/health/health_api.dart';

// Esta clase ejecuta la verificación de disponibilidad del servidor.
class CheckBackendHealthUseCase {
  const CheckBackendHealthUseCase(this._repository);
  final HealthRepository _repository;
  Future<HealthStatus> call() => _repository.check();
}
