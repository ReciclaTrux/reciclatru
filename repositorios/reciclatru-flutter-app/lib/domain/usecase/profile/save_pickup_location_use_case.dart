/// Encapsula el caso de uso save pickup location use case y delega la operación en los contratos del dominio.
import '../../repository/profile/profile_repository.dart';
import '../../../model/profile/pickup_location.dart';

/// Guarda el punto y los datos de dirección confirmados por el vecino.
class SavePickupLocationUseCase {
  const SavePickupLocationUseCase(this._repository);
  final ProfileRepository _repository;
  Future<PickupLocation> call(PickupLocation location) => _repository.savePickupLocation(location);
}
