// Este modelo guarda la información completa de un pedido publicado por un vecino (sus materiales, estado, fotos y datos de contacto).
import 'order_item.dart';

export 'order_acceptance.dart';
export 'order_item.dart';
export 'order_summary.dart';

// Este listado representa los posibles estados de un pedido (pendiente, confirmado, recolectado o cancelado).
enum OrderStatus { pending, confirmed, collected, cancelled }

// Esta clase guarda los datos de una foto adjunta al pedido.
class OrderPhoto {
  const OrderPhoto({required this.id, required this.url});
  final String id;
  final String url;

  factory OrderPhoto.fromJson(Map<String, dynamic> json) => OrderPhoto(
        id: json['id']?.toString() ?? '',
        url: json['url']?.toString() ?? '',
      );
}

// Esta clase guarda el nombre y teléfono de contacto cuando la otra persona ya puede verlos.
class OrderContact {
  const OrderContact({required this.fullName, required this.phone});
  final String fullName;
  final String phone;
}

// Esta clase guarda toda la información detallada de un pedido de reciclaje.
class Order {
  const Order({
    required this.id,
    required this.status,
    required this.items,
    required this.totalWeightKg,
    this.acceptanceCount,
    this.chosenRecyclerName,
    this.createdAt,
    this.counterpart,
    this.canCancel = false,
    this.canChooseRecycler = false,
    this.canEdit = false,
    this.photos = const [],
  });

  final String id;
  final OrderStatus status;
  final List<OrderItem> items;
  final double totalWeightKg;
  final int? acceptanceCount;
  final String? chosenRecyclerName;
  final DateTime? createdAt;
  final OrderContact? counterpart;
  final bool canCancel;
  final bool canChooseRecycler;
  final bool canEdit;
  final List<OrderPhoto> photos;

  // Esta función convierte la respuesta recibida del servidor en una estructura de pedido comprensible por la aplicación.
  factory Order.fromRecord(Map<String, dynamic> json) {
    final rawItems = json['items'];
    final rawPhotos = json['photos'];
    final rawActions = json['availableActions'];
    final contact = json['counterpart'] ?? json['recycler'] ?? json['neighbor'];
    final status = _parseOrderStatus(json['status']);
    return Order(
      id: json['id']?.toString() ?? '',
      status: status,
      items: rawItems is List
          ? rawItems.whereType<Map>().map((item) => OrderItem.fromJson(
                Map<String, dynamic>.from(item),
              )).toList()
          : const [],
      totalWeightKg: (json['totalWeightKg'] as num?)?.toDouble() ?? 0,
      acceptanceCount: (json['acceptanceCount'] as num?)?.toInt(),
      chosenRecyclerName: json['chosenRecyclerName']?.toString(),
      createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? ''),
      counterpart: contact is Map
          ? OrderContact(
              fullName: contact['fullName']?.toString() ?? '',
              phone: contact['phone']?.toString() ?? '',
            )
          : null,
      canCancel: rawActions is List
          ? rawActions.contains('CANCEL')
          : status == OrderStatus.pending || status == OrderStatus.confirmed,
      canChooseRecycler: rawActions is List
          ? rawActions.contains('CHOOSE_RECYCLER')
          : status == OrderStatus.pending && ((json['acceptanceCount'] as num?)?.toInt() ?? 0) > 0,
      canEdit: rawActions is List
          ? rawActions.contains('EDIT')
          : status == OrderStatus.pending && ((json['acceptanceCount'] as num?)?.toInt() ?? 0) == 0,
      photos: rawPhotos is List
          ? rawPhotos.whereType<Map>().map((photo) => OrderPhoto.fromJson(
                Map<String, dynamic>.from(photo),
              )).toList()
          : const [],
    );
  }
}

// Esta función interpreta el texto del estado recibido del servidor y lo transforma al estado oficial del pedido.
OrderStatus _parseOrderStatus(Object? value) => switch (value?.toString().toUpperCase()) {
      'PENDING' => OrderStatus.pending,
      'CONFIRMED' => OrderStatus.confirmed,
      'COLLECTED' => OrderStatus.collected,
      'CANCELLED' => OrderStatus.cancelled,
      _ => throw const FormatException('Estado de pedido desconocido.'),
    };
