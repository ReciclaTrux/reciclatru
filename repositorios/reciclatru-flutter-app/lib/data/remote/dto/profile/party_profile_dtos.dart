/// DTOs de consulta y actualización de datos personales.
class ProfileUpdateRequestDto {
  const ProfileUpdateRequestDto({required this.fullName, required this.phone, this.reference});
  final String fullName;
  final String phone;
  final String? reference;
  Map<String, dynamic> toJson() => {
        'fullName': fullName,
        'phone': phone,
        if (reference != null && reference!.isNotEmpty) 'reference': reference,
      };
}

class PartyProfileOutputDto {
  const PartyProfileOutputDto(this.json);
  final Map<String, dynamic> json;
  factory PartyProfileOutputDto.fromJson(Map<String, dynamic> json) => PartyProfileOutputDto(json);
}
