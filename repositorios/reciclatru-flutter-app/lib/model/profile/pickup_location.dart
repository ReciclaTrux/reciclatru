// Este modelo guarda la dirección, el distrito y el punto del mapa donde el reciclador recogerá el pedido.
import 'geo_point.dart';

// Esta clase contiene los datos de la calle y distrito leídos en el mapa.
class PickupAddress {
  const PickupAddress({
    required this.address,
    required this.district,
    this.latitude,
    this.longitude,
  });

  final String address;
  final String district;
  final double? latitude;
  final double? longitude;
}

// Esta clase guarda las coordenadas y la dirección completa del punto de recojo elegido por el vecino.
class PickupLocation {
  const PickupLocation({
    required this.point,
    required this.address,
    required this.district,
    this.referenceNote,
    this.id,
  });

  final GeoPoint point;
  final String address;
  final String district;
  final String? referenceNote;
  final String? id;
}
