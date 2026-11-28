// Este modelo guarda la descripción del material a reciclar y su peso estimado en kilogramos.
class OrderItem {
  const OrderItem({required this.description, required this.weightKg});
  final String description;
  final double weightKg;

  // Esta función lee los datos del material desde el formato recibido del servidor.
  factory OrderItem.fromJson(Map<String, dynamic> json) => OrderItem(
        description: json['description']?.toString() ?? '',
        weightKg: (json['weightKg'] as num?)?.toDouble() ?? 0,
      );

  // Esta función convierte la descripción y el peso al formato de envío requerido por el servidor.
  Map<String, dynamic> toJson() => {
        'description': description,
        'weightKg': weightKg,
      };
}
