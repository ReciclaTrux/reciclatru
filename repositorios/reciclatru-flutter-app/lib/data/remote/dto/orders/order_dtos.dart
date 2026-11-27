/// Define los objetos de transferencia de datos de order dtos usados al comunicarse con la API.
import '../../../../model/orders/order.dart';
import '../../../../model/orders/order_summary.dart';

/// DTO de cada material enviado o recibido por la API.
class OrderItemDto {
  const OrderItemDto({this.description, this.weightKg});
  final String? description;
  final double? weightKg;
  factory OrderItemDto.fromJson(Map<String, dynamic> json) => OrderItemDto(
    description: json['description']?.toString(),
    weightKg: (json['weightKg'] as num?)?.toDouble(),
  );
  Map<String, dynamic> toJson() => {'description': description, 'weightKg': weightKg};
}

class ServicingOrderInitiateRequestDto {
  const ServicingOrderInitiateRequestDto(this.items);
  final List<OrderItemDto> items;
  Map<String, dynamic> toJson() => {'items': items.map((item) => item.toJson()).toList()};
}

class ServicingOrderUpdateRequestDto extends ServicingOrderInitiateRequestDto {
  const ServicingOrderUpdateRequestDto(super.items);
}

class ServicingOrderOutputDto {
  const ServicingOrderOutputDto(this.record);
  final Map<String, dynamic> record;
  factory ServicingOrderOutputDto.fromJson(Map<String, dynamic> json) {
    final record = json['servicingOrderInstanceRecord'];
    if (record is! Map) throw const FormatException('No pudimos completar la operación. Inténtalo de nuevo.');
    return ServicingOrderOutputDto(Map<String, dynamic>.from(record));
  }
}

class ServicingOrderListOutputDto {
  const ServicingOrderListOutputDto({required this.items, this.nextCursor});
  final List<OrderSummary> items;
  final String? nextCursor;
}

class OrderPhotoDto {
  const OrderPhotoDto(this.photo);
  final OrderPhoto photo;
  factory OrderPhotoDto.fromJson(Map<String, dynamic> json) => OrderPhotoDto(OrderPhoto.fromJson(json));
}

class ChooseRecyclerRequestDto {
  const ChooseRecyclerRequestDto(this.acceptanceId);
  final String acceptanceId;
  Map<String, dynamic> toJson() => {'acceptanceId': acceptanceId};
}

/// Traducción de la respuesta con aceptaciones de recicladores.
class AcceptanceListOutputDto {
  const AcceptanceListOutputDto(this.items);
  final List<OrderAcceptance> items;
}
