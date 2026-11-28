// Este contrato define todas las operaciones sobre los pedidos del vecino (crear, editar, cancelar, ver detalle y elegir reciclador).
import '../../../model/orders/order.dart';

// Esta interfaz especifica la gestión completa de pedidos para los usuarios.
abstract interface class OrdersRepository {
  Future<OrdersPage> getMyOrders({required String tab, String? cursor});
  Future<Order?> getOrder(String id);
  Future<Order> createOrder(List<OrderItem> items);
  Future<Order> updateOrder(String id, List<OrderItem> items);
  Future<Order> cancelOrder(String id);
  Future<List<OrderAcceptance>> getAcceptances(String id);
  Future<Order> chooseRecycler(String id, String acceptanceId);
  Future<OrderPhoto> uploadPhoto(String orderId, String filePath);
  Future<void> removePhoto(String orderId, String photoId);
}
