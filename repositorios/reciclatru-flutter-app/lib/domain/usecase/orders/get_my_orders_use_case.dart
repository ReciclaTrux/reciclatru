/// Encapsula el caso de uso get my orders use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order_summary.dart';

/// Recupera la pestaña activa o el historial de pedidos.
class GetMyOrdersUseCase {
  const GetMyOrdersUseCase(this._repository);
  final OrdersRepository _repository;
  Future<OrdersPage> call(OrdersTab tab, [String? cursor]) =>
      _repository.getMyOrders(tab: tab.name.toUpperCase(), cursor: cursor);
}
