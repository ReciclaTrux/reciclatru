/// Respuesta paginada de pedidos disponibles y registro de detalle.
class AvailableOrdersOutputDto {
  const AvailableOrdersOutputDto({required this.items, this.nextCursor, this.sortedBy});
  final List<Map<String, dynamic>> items;
  final String? nextCursor;
  final String? sortedBy;
  factory AvailableOrdersOutputDto.fromJson(Map<String, dynamic> json) {
    final rows = json['items'];
    return AvailableOrdersOutputDto(
      items: rows is List ? rows.whereType<Map>().map((row) => Map<String, dynamic>.from(row)).toList() : const [],
      nextCursor: json['nextCursor']?.toString(),
      sortedBy: json['sortedBy']?.toString(),
    );
  }
}

class AvailableOrderDetailOutputDto {
  const AvailableOrderDetailOutputDto(this.record);
  final Map<String, dynamic> record;
  factory AvailableOrderDetailOutputDto.fromJson(Map<String, dynamic> json) {
    final record = json['servicingOrderInstanceRecord'];
    if (record is! Map) throw const FormatException('No pudimos completar la operación. Inténtalo de nuevo.');
    return AvailableOrderDetailOutputDto(Map<String, dynamic>.from(record));
  }
}
