/// Encapsula el caso de uso create order use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

/// Publica un pedido con los materiales ingresados por el vecino.
class CreateOrderUseCase {
  const CreateOrderUseCase(this._repository);
  final OrdersRepository _repository;
  Future<Order> call(List<OrderItem> items) => _repository.createOrder(items);
}
