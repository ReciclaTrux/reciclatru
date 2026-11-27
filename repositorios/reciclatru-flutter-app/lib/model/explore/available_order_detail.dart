// Este modelo guarda la información detallada de un pedido disponible para que el reciclador revise antes de aceptarlo.
import '../orders/order.dart';
import '../orders/order_acceptance.dart';
import '../orders/order_item.dart';

// Esta clase contiene el detalle completo del pedido visible para los recicladores.
class AvailableOrderDetail {
  const AvailableOrderDetail({
    required this.id,
    required this.status,
    required this.zone,
    required this.items,
    required this.totalWeightKg,
    required this.distanceKm,
    required this.createdAt,
    required this.acceptanceCount,
    required this.acceptanceStatus,
    required this.photos,
    required this.neighbor,
  });

  final String id;
  final OrderStatus status;
  final String zone;
  final List<OrderItem> items;
  final double totalWeightKg;
  final double? distanceKm;
  final DateTime? createdAt;
  final int acceptanceCount;
  final AcceptanceStatus? acceptanceStatus;
  final List<OrderPhoto> photos;
  final OrderContact? neighbor;

  // Esta propiedad indica si el reciclador actual ya aceptó este pedido.
  bool get alreadyAccepted => acceptanceStatus != null;

  // Esta función convierte la respuesta en formato JSON recibida del servidor en este modelo detallado.
  factory AvailableOrderDetail.fromRecord(Map<String, dynamic> json) {
    final rawItems = json['items'];
    final rawPhotos = json['photos'];
    final contact = json['counterpart'];
    final rawAcceptance = json['acceptanceStatus']?.toString();
    final acceptance = switch (rawAcceptance) {
      'ACCEPTED' => AcceptanceStatus.accepted,
      'CHOSEN' => AcceptanceStatus.chosen,
      'DISCARDED' => AcceptanceStatus.discarded,
      _ => json['alreadyAccepted'] == true ? AcceptanceStatus.accepted : null,
    };
    final items = rawItems is List
        ? rawItems.whereType<Map>().map((item) => OrderItem.fromJson(
              Map<String, dynamic>.from(item),
            )).toList()
        : const <OrderItem>[];
    return AvailableOrderDetail(
      id: json['id']?.toString() ?? '',
      status: _parseOrderStatus(json['status']),
      zone: json['zone']?.toString() ?? '',
      items: items,
      totalWeightKg: (json['totalWeightKg'] as num?)?.toDouble() ??
          items.fold<double>(0, (sum, item) => sum + item.weightKg),
      distanceKm: (json['approximateDistanceKm'] as num?)?.toDouble(),
      createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? ''),
      acceptanceCount: (json['acceptanceCount'] as num?)?.toInt() ?? 0,
      acceptanceStatus: acceptance,
      photos: rawPhotos is List
          ? rawPhotos.whereType<Map>().map((photo) => OrderPhoto.fromJson(
                Map<String, dynamic>.from(photo),
              )).toList()
          : const [],
      neighbor: contact is Map
          ? OrderContact(
              fullName: contact['fullName']?.toString() ?? '',
              phone: contact['phone']?.toString() ?? '',
            )
          : null,
    );
  }
}

// Esta función convierte el texto de estado que envía el servidor al estado reconocible por la aplicación.
OrderStatus _parseOrderStatus(Object? value) => switch (value?.toString()) {
      'PENDING' => OrderStatus.pending,
      'CONFIRMED' => OrderStatus.confirmed,
      'COLLECTED' => OrderStatus.collected,
      'CANCELLED' => OrderStatus.cancelled,
      _ => throw const FormatException('Estado de pedido desconocido.'),
    };
