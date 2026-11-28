/// Encapsula el caso de uso describe point use case y delega la operación en los contratos del dominio.
import '../../repository/profile/geocoding_repository.dart';
import '../../../model/profile/geo_point.dart';
import '../../../model/profile/pickup_location.dart';

/// Resuelve calle y distrito para el pin confirmado en el mapa.
class DescribePointUseCase {
  const DescribePointUseCase(this._repository);
  final GeocodingRepository _repository;
  Future<PickupAddress?> call(GeoPoint point) => _repository.describePoint(point);
}
