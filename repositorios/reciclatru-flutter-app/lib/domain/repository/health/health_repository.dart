// Este contrato define la función para comprobar si los servicios del servidor están funcionando correctamente.
import '../../../data/remote/api/health/health_api.dart';

// Esta interfaz especifica la verificación del estado de salud del servidor.
abstract interface class HealthRepository {
  Future<HealthStatus> check();
}
