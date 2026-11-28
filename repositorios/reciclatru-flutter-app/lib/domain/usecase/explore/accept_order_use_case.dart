// Este caso de uso permite al reciclador confirmar que desea recoger un pedido disponible.
import '../../repository/explore/explore_repository.dart';
import '../../../model/explore/available_order_detail.dart';

// Esta clase ejecuta la aceptación del pedido seleccionado.
class AcceptOrderUseCase {
  const AcceptOrderUseCase(this._repository);
  final ExploreRepository _repository;
  Future<AvailableOrderDetail> call(String id) => _repository.acceptOrder(id);
}
