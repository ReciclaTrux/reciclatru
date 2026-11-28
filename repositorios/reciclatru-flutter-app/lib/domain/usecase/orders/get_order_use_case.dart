/// Encapsula el caso de uso get order use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

/// Carga el detalle de un pedido propio.
class GetOrderUseCase {
  const GetOrderUseCase(this._repository);
  final OrdersRepository _repository;
  Future<Order?> call(String id) => _repository.getOrder(id);
}
