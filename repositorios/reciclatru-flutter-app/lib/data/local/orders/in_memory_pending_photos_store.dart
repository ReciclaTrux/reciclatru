/// Implementa el acceso local de in memory pending photos store para persistencia o servicios del dispositivo.
import '../../../domain/repository/orders/pending_photos_store.dart';

/// Conserva en memoria las fotos pendientes, como la implementación Kotlin.
class InMemoryPendingPhotosStore implements PendingPhotosStore {
  final Map<String, List<String>> _photosByOrder = {};

  @override
  List<String> get(String orderId) => List.unmodifiable(_photosByOrder[orderId] ?? const []);

  @override
  void put(String orderId, List<String> photos) {
    if (photos.isEmpty) {
      _photosByOrder.remove(orderId);
    } else {
      _photosByOrder[orderId] = List.unmodifiable(photos);
    }
  }
}
