/// Encapsula el caso de uso search address use case y delega la operación en los contratos del dominio.
import '../../repository/profile/geocoding_repository.dart';
import '../../../model/profile/geo_point.dart';

/// Busca una dirección y devuelve su coordenada sin consultar texto vacío.
class SearchAddressUseCase {
  const SearchAddressUseCase(this._repository);
  final GeocodingRepository _repository;
  bool get isAvailable => _repository.isAvailable;
  Future<GeoPoint?> call(String query) {
    final value = query.trim();
    return value.isEmpty ? Future.value(null) : _repository.searchAddress(value);
  }
}
