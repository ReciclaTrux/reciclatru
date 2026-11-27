/// Respuesta del endpoint de salud de la API.
class HealthDto {
  const HealthDto({this.status, this.version});
  final String? status;
  final String? version;
  factory HealthDto.fromJson(Map<String, dynamic> json) => HealthDto(
    status: json['status']?.toString(),
    version: json['version']?.toString(),
  );
}
