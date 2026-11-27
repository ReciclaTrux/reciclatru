// Este modelo guarda un resumen breve de un pedido para mostrarlo en las listas de pedidos del vecino.
import 'order.dart';

// Este listado representa las pestañas disponibles en la lista de pedidos del vecino (activos o historial).
enum OrdersTab { active, history }

// Esta clase guarda los datos resumidos del pedido (cantidad de materiales, peso total, foto miniatura y estado).
class OrderSummary {
  const OrderSummary({
    required this.id,
    required this.status,
    required this.itemCount,
    required this.totalWeightKg,
    this.acceptanceCount,
    this.chosenRecyclerName,
    this.createdAt,
    this.thumbnailUrl,
  });

  final String id;
  final OrderStatus status;
  final int itemCount;
  final double totalWeightKg;
  final int? acceptanceCount;
  final String? chosenRecyclerName;
  final DateTime? createdAt;
  final String? thumbnailUrl;

  // Esta función lee los datos del resumen entregados por el servidor.
  factory OrderSummary.fromJson(Map<String, dynamic> json) => OrderSummary(
        id: json['id']?.toString() ?? '',
        status: _parseOrderStatus(json['status']),
        itemCount: (json['itemCount'] as num?)?.toInt() ?? 0,
        totalWeightKg: (json['totalWeightKg'] as num?)?.toDouble() ?? 0,
        acceptanceCount: (json['acceptanceCount'] as num?)?.toInt(),
        chosenRecyclerName: json['chosenRecyclerName']?.toString(),
        createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? ''),
        thumbnailUrl: json['thumbnailUrl']?.toString(),
      );
}

// Esta clase guarda una página de resúmenes de pedidos para la lista paginada.
class OrdersPage {
  const OrdersPage({required this.items, this.nextCursor});
  final List<OrderSummary> items;
  final String? nextCursor;
}

// Esta función traduce el estado textual enviado por el servidor al estado enum del resumen.
OrderStatus _parseOrderStatus(Object? value) => switch (value?.toString().toUpperCase()) {
      'PENDING' => OrderStatus.pending,
      'CONFIRMED' => OrderStatus.confirmed,
      'COLLECTED' => OrderStatus.collected,
      'CANCELLED' => OrderStatus.cancelled,
      _ => throw const FormatException('Estado de pedido desconocido.'),
    };
