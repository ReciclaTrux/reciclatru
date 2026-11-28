/// Implementa el repositorio profile repository impl conectando los contratos del dominio con sus fuentes de datos.

import '../../../domain/repository/profile/profile_repository.dart';
import '../../../model/profile/pickup_location.dart';
import '../../remote/api/profile/profile_api.dart';
import '../../remote/dto/profile/pickup_point_dtos.dart';
import '../../remote/mapper/profile/pickup_location_mapper.dart';
import '../../remote/network_exception.dart';

/// Guarda únicamente el punto de recojo, como `ProfileRepositoryImpl.kt`.
class ProfileRepositoryImpl implements ProfileRepository {
  const ProfileRepositoryImpl(this._api);
  final ProfileApi _api;

  @override
  Future<PickupLocation> savePickupLocation(PickupLocation location) async {
    final response = await apiCall(() => _api.updatePickupPoint(PickupPointRequestDto(
      latitude: location.point.latitude,
      longitude: location.point.longitude,
      address: location.address,
      district: location.district,
    )));
    if (response.json['partyDataManagementInstanceRecord'] is! Map) {
      throw const FormatException('El servidor no devolvió el perfil actualizado.');
    }
    return mapPickupLocation(response, fallback: location);
  }
}

