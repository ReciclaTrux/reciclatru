/// Equivalente de UserProfile.kt y PickupAddress de Android.
class PickupAddress {
  const PickupAddress({required this.address, required this.district});
  final String address;
  final String district;
}

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
  final String role; // NEIGHBOR o RECYCLER
  final String? fullName;
  final String? phone;
  final String? reference;
  final PickupAddress? pickupAddress;
  final bool profileComplete;

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
    if (pickup is Map && pickup['latitude'] is num && pickup['longitude'] is num) {
      pickupAddress = PickupAddress(
        address: pickup['address']?.toString() ?? '',
        district: pickup['district']?.toString() ?? '',
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
