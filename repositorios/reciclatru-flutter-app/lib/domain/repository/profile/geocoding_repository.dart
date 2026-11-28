// Este contrato define la búsqueda de direcciones y la conversión de puntos del mapa en texto legible.
import '../../../model/profile/geo_point.dart';
import '../../../model/profile/pickup_location.dart';

// Esta clase representa una opción de dirección sugerida con sus coordenadas.
class AddressSuggestion {
  const AddressSuggestion({required this.label, required this.point});
  final String label;
  final GeoPoint point;
}

// Esta interfaz especifica las funciones de búsqueda y descripción de ubicaciones.
abstract interface class GeocodingRepository {
  bool get isAvailable;

  Future<void> initialize();

  Future<List<AddressSuggestion>> suggestAddresses(String query);
  Future<GeoPoint?> searchAddress(String query);
  Future<PickupAddress?> describePoint(GeoPoint point);
}
