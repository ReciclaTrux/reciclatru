/// Coordenadas y etiquetas enviadas para guardar un punto de recojo.
class PickupPointRequestDto {
  const PickupPointRequestDto({required this.latitude, required this.longitude, required this.address, required this.district});
  final double latitude;
  final double longitude;
  final String address;
  final String district;
  Map<String, dynamic> toJson() => {
    'latitude': latitude,
    'longitude': longitude,
    'address': address,
    'district': district,
  };
}
