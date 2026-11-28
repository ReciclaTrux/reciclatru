// Este caso de uso permite al vecino cancelar un pedido publicado.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';

// Esta clase ejecuta la cancelación del pedido indicado.
class CancelOrderUseCase {
  const CancelOrderUseCase(this._repository);
  final OrdersRepository _repository;
  Future<Order> call(String id) => _repository.cancelOrder(id);
}
