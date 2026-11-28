/// Encapsula el caso de uso suggest addresses use case y delega la operación en los contratos del dominio.
import '../../repository/profile/geocoding_repository.dart';

/// Solicita sugerencias del autocomplete al Geocoder del sistema.
class SuggestAddressesUseCase {
  const SuggestAddressesUseCase(this._repository);
  final GeocodingRepository _repository;
  Future<List<AddressSuggestion>> call(String query) {
    final value = query.trim();
    return value.isEmpty ? Future.value(const []) : _repository.suggestAddresses(value);
  }
}
