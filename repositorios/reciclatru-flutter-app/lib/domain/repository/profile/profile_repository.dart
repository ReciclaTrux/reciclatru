// Este contrato define la función para guardar el punto de recojo elegido en el mapa.
import '../../../model/profile/pickup_location.dart';

// Esta interfaz especifica el guardado de la ubicación de recojo.
abstract interface class ProfileRepository {
  Future<PickupLocation> savePickupLocation(PickupLocation location);
}
