/// Transforma la respuesta de explore mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/explore/available_order.dart';
import '../../api_urls.dart';
import '../../dto/explore/explore_dtos.dart';

/// Mapea página de DTOs y conserva el orden decidido por el servidor.
AvailableOrdersPage mapAvailableOrders(AvailableOrdersOutputDto dto) {
  final items = dto.items
      // Kotlin descarta filas sin identificador y pedidos que ya no están pendientes.
      .where((row) =>
          row['id'] != null &&
          row['id'].toString().trim().isNotEmpty &&
          (row['status'] == null || row['status'] == 'PENDING'))
      // Las rutas de fotos pueden venir completas o solo con la ruta del servidor.
      .map((row) => AvailableOrder.fromJson(withAbsoluteApiImageUrls(row)))
      .toList();
  return AvailableOrdersPage(
    items: items,
    nextCursor: dto.nextCursor,
    sortedByDistance: dto.sortedBy == 'DISTANCE',
  );
}
