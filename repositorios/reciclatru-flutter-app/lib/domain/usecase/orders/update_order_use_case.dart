/// Encapsula el caso de uso update order use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

/// Actualiza los materiales de un pedido cuando la API lo permite.
class UpdateOrderUseCase {
  const UpdateOrderUseCase(this._repository);
  final OrdersRepository _repository;
  Future<Order> call(String id, List<OrderItem> items) => _repository.updateOrder(id, items);
}
