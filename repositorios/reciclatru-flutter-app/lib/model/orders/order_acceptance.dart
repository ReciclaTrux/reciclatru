// Este modelo guarda la información de un reciclador que aceptó la recolección de un pedido.
enum AcceptanceStatus { accepted, chosen, discarded }

// Esta clase contiene el nombre, teléfono y estado de aceptación del reciclador interesado en el pedido.
class OrderAcceptance {
  const OrderAcceptance({
    required this.id,
    required this.status,
    required this.fullName,
    required this.phone,
    this.acceptedAt,
  });

  final String id;
  final AcceptanceStatus status;
  final String fullName;
  final String phone;
  final DateTime? acceptedAt;
}
