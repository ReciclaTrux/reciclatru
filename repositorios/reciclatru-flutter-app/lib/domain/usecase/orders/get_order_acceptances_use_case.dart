/// Encapsula el caso de uso get order acceptances use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

/// Obtiene las aceptaciones que el vecino puede revisar.
class GetOrderAcceptancesUseCase {
  const GetOrderAcceptancesUseCase(this._repository);
  final OrdersRepository _repository;
  Future<List<OrderAcceptance>> call(String id) => _repository.getAcceptances(id);
}
