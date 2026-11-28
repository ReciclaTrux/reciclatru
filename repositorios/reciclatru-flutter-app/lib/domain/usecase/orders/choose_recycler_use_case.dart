/// Encapsula el caso de uso choose recycler use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

/// Confirma qué aceptación fue elegida por el vecino.
class ChooseRecyclerUseCase {
  const ChooseRecyclerUseCase(this._repository);
  final OrdersRepository _repository;
  Future<Order> call(String orderId, String acceptanceId) =>
      _repository.chooseRecycler(orderId, acceptanceId);
}
