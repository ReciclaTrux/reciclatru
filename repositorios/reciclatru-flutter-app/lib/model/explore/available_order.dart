// Este modelo guarda la información básica de un pedido disponible que el reciclador puede ver en su lista.
class GeoPoint {
  const GeoPoint({required this.latitude, required this.longitude});
  final double latitude;
  final double longitude;
}

// Esta clase representa la información del pedido visible en la lista del reciclador.
class AvailableOrder {
  const AvailableOrder({
    required this.id,
    required this.zone,
    required this.itemCount,
    required this.totalWeightKg,
    required this.distanceKm,
    required this.alreadyAccepted,
    required this.createdAt,
    this.thumbnailUrl,
  });

  final String id;
  final String zone;
  final int itemCount;
  final double totalWeightKg;
  final double? distanceKm;
  final bool alreadyAccepted;
  final DateTime? createdAt;
  final String? thumbnailUrl;

  // Esta función convierte la respuesta recibida del servidor al modelo de datos que usa la pantalla.
  factory AvailableOrder.fromJson(Map<String, dynamic> json) => AvailableOrder(
        id: json['id']?.toString() ?? '',
        zone: json['zone']?.toString() ?? '',
        itemCount: (json['itemCount'] as num?)?.toInt() ?? 0,
        totalWeightKg: (json['totalWeightKg'] as num?)?.toDouble() ?? 0,
        distanceKm: (json['approximateDistanceKm'] as num?)?.toDouble(),
        alreadyAccepted: json['alreadyAccepted'] == true,
        createdAt: DateTime.tryParse(json['createdAt']?.toString() ?? ''),
        thumbnailUrl: json['thumbnailUrl']?.toString(),
      );
}

// Esta clase guarda una página de la lista de pedidos disponibles junto con la opción de cargar más.
class AvailableOrdersPage {
  const AvailableOrdersPage({
    required this.items,
    this.nextCursor,
    required this.sortedByDistance,
  });
  final List<AvailableOrder> items;
  final String? nextCursor;
  final bool sortedByDistance;
}
