/// Transforma la respuesta de available order detail mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/explore/available_order_detail.dart';
import '../../api_urls.dart';
import '../../dto/explore/explore_dtos.dart';

/// Convierte el registro privado del endpoint a detalle público de exploración.
AvailableOrderDetail mapAvailableOrderDetail(AvailableOrderDetailOutputDto dto) {
  // Algunos registros antiguos usan crReferenceId en lugar de id.
  final id = dto.record['id'] ?? dto.record['crReferenceId'];
  if (id == null || id.toString().trim().isEmpty) {
    throw const FormatException('El pedido no tiene un identificador válido.');
  }

  final record = withAbsoluteApiImageUrls({
    ...dto.record,
    'id': id,
  });

  return AvailableOrderDetail.fromRecord(record);
}
