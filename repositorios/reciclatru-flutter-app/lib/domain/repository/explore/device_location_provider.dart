// Este contrato define la función para obtener las coordenadas geográficas actuales del dispositivo.
import '../../../model/explore/available_order.dart';

// Esta interfaz se encarga de proveer la ubicación actual del teléfono cuando se consulta la lista de pedidos.
abstract interface class DeviceLocationProvider {
  Future<GeoPoint?> currentLocation();
}
