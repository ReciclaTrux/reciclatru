/// Transforma la respuesta de pickup location mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/profile/geo_point.dart';
import '../../../../model/profile/pickup_location.dart';
import '../../dto/profile/party_profile_dtos.dart';

/// Obtiene el punto guardado; si el backend no devuelve un punto, conserva el enviado.
PickupLocation mapPickupLocation(
  PartyProfileOutputDto dto, {
  required PickupLocation fallback,
}) {
  final record = dto.json['partyDataManagementInstanceRecord'];
  final pickup = record is Map ? record['pickupPoint'] : null;
  if (pickup is! Map) {
    return fallback;
  }
  final point = Map<String, dynamic>.from(pickup);
  return PickupLocation(
    point: GeoPoint(
      latitude: (point['latitude'] as num?)?.toDouble() ?? fallback.point.latitude,
      longitude: (point['longitude'] as num?)?.toDouble() ?? fallback.point.longitude,
    ),
    address: point['address']?.toString().trim().isNotEmpty == true
        ? point['address'].toString()
        : fallback.address,
    district: point['district']?.toString().trim().isNotEmpty == true
        ? point['district'].toString()
        : fallback.district,
    referenceNote: fallback.referenceNote,
  );
}
