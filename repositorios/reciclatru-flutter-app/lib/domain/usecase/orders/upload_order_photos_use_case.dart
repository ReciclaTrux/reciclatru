/// Encapsula el caso de uso upload order photos use case y delega la operación en los contratos del dominio.
import 'package:dio/dio.dart';

import '../../repository/orders/orders_repository.dart';
import '../../../model/orders/order.dart';
import '../../repository/orders/pending_photos_store.dart';

/// Sube en orden las fotos temporales elegidas antes de publicar.
class UploadOrderPhotosUseCase {
  const UploadOrderPhotosUseCase(this._repository, this._pendingPhotos);
  final OrdersRepository _repository;
  final PendingPhotosStore _pendingPhotos;
  Future<int> call(String orderId, List<String> paths) async {
    final failed = <String>[];
    for (final path in paths) {
      try {
        await _repository.uploadPhoto(orderId, path);
      } on DioException catch (error) {
        if (error.type == DioExceptionType.cancel) rethrow;
        failed.add(path);
      } catch (_) {
        failed.add(path);
      }
    }
    _pendingPhotos.put(orderId, failed);
    return failed.length;
  }
}


