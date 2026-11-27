// Este caso de uso consulta la información detallada de un pedido disponible para el reciclador.
import '../../repository/explore/explore_repository.dart';
import '../../../model/explore/available_order.dart';
import '../../../model/explore/available_order_detail.dart';

// Esta clase obtiene el detalle completo del pedido seleccionado en la lista de exploración.
class GetAvailableOrderDetailUseCase {
  const GetAvailableOrderDetailUseCase(this._repository);
  final ExploreRepository _repository;
  Future<AvailableOrderDetail> call(String id, GeoPoint? location) => _repository.getOrderDetail(id, location);
}
