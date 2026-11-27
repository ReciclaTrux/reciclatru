// Este contrato define las operaciones que permite consultar y aceptar pedidos disponibles para los recicladores.
import '../../../model/explore/available_order.dart';
import '../../../model/explore/available_order_detail.dart';

// Esta interfaz especifica cómo obtener la lista de pedidos en la zona, ver su detalle y aceptarlos.
abstract interface class ExploreRepository {
  Future<AvailableOrdersPage> getAvailableOrders(GeoPoint? location, {String? cursor});
  Future<AvailableOrderDetail> getOrderDetail(String id, GeoPoint? location);
  Future<AvailableOrderDetail> acceptOrder(String id);
}
