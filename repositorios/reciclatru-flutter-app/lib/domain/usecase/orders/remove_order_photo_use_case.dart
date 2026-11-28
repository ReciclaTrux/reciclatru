/// Encapsula el caso de uso remove order photo use case y delega la operación en los contratos del dominio.
import '../../repository/orders/orders_repository.dart';

/// Elimina una foto del pedido por medio del endpoint existente.
class RemoveOrderPhotoUseCase {
  const RemoveOrderPhotoUseCase(this._repository);
  final OrdersRepository _repository;
  Future<void> call(String orderId, String photoId) =>
      _repository.removePhoto(orderId, photoId);
}
