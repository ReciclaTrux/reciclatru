// Este modelo guarda la información personal del usuario (correo, rol, nombre, teléfono y dirección de recojo).
import 'pickup_location.dart';

export 'pickup_location.dart';

// Esta clase guarda todos los datos del perfil que el servidor devuelve para la sesión iniciada.
class UserProfile {
  const UserProfile({
    required this.email,
    required this.role,
    required this.fullName,
    required this.phone,
    required this.reference,
    required this.pickupAddress,
    required this.profileComplete,
  });

  final String email;
  final String role;
  final String? fullName;
  final String? phone;
  final String? reference;
  final PickupAddress? pickupAddress;
  final bool profileComplete;

  // Esta función construye el objeto de perfil a partir de la respuesta en formato JSON enviada por el servidor.
  factory UserProfile.fromJson(Map<String, dynamic> response) {
    final raw = response['partyDataManagementInstanceRecord'];
    if (raw is! Map) {
      throw const FormatException('No pudimos completar la operación. Inténtalo de nuevo.');
    }
    final record = Map<String, dynamic>.from(raw);
    final role = record['role'];
    if (role != 'NEIGHBOR' && role != 'RECYCLER') {
      throw const FormatException('No pudimos completar la operación. Inténtalo de nuevo.');
    }

    PickupAddress? pickupAddress;
    final pickup = record['pickupPoint'];
    if (pickup is Map) {
      pickupAddress = PickupAddress(
        address: pickup['address']?.toString() ?? '',
        district: pickup['district']?.toString() ?? '',
        latitude: (pickup['latitude'] as num?)?.toDouble(),
        longitude: (pickup['longitude'] as num?)?.toDouble(),
      );
    }

    return UserProfile(
      email: record['email']?.toString() ?? '',
      role: role as String,
      fullName: record['fullName']?.toString(),
      phone: record['phone']?.toString(),
      reference: record['reference']?.toString(),
      pickupAddress: pickupAddress,
      profileComplete: record['profileComplete'] == true,
    );
  }
}
