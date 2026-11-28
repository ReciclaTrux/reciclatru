// Este caso de uso consulta la lista de pedidos disponibles cerca de la posición del reciclador.
import '../../repository/explore/explore_repository.dart';
import '../../../model/explore/available_order.dart';

// Esta clase obtiene las ofertas de pedidos disponibles para ser recogidos.
class GetAvailableOrdersUseCase {
  const GetAvailableOrdersUseCase(this._repository);
  final ExploreRepository _repository;
  Future<AvailableOrdersPage> call(GeoPoint? location, {String? cursor}) =>
      _repository.getAvailableOrders(location, cursor: cursor);
}
