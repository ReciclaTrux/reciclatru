/// DTOs JSON de registro y autenticación, equivalentes a `AuthDtos.kt`.
class RegisterRequestDto {
  const RegisterRequestDto({required this.email, required this.password, required this.role});
  final String email;
  final String password;
  final String role;
  Map<String, dynamic> toJson() => {'email': email, 'password': password, 'role': role};
}

class LoginRequestDto {
  const LoginRequestDto({required this.email, required this.password});
  final String email;
  final String password;
  Map<String, dynamic> toJson() => {'email': email, 'password': password};
}

class PartyRecordDto {
  const PartyRecordDto({this.id, this.email, this.role, this.profileComplete});
  final String? id;
  final String? email;
  final String? role;
  final bool? profileComplete;
  factory PartyRecordDto.fromJson(Map<String, dynamic> json) => PartyRecordDto(
    id: json['id']?.toString(),
    email: json['email']?.toString(),
    role: json['role']?.toString(),
    profileComplete: json['profileComplete'] as bool?,
  );
}

class PartyDataManagementOutputDto {
  const PartyDataManagementOutputDto({this.crReferenceId, this.record});
  final String? crReferenceId;
  final PartyRecordDto? record;
  factory PartyDataManagementOutputDto.fromJson(Map<String, dynamic> json) => PartyDataManagementOutputDto(
    crReferenceId: json['crReferenceId']?.toString(),
    record: json['partyDataManagementInstanceRecord'] is Map
        ? PartyRecordDto.fromJson(Map<String, dynamic>.from(json['partyDataManagementInstanceRecord'] as Map))
        : null,
  );
}

class PartyAuthenticationOutputDto {
  const PartyAuthenticationOutputDto({this.crReferenceId, this.accessToken, this.tokenType, this.expiresAt, this.record});
  final String? crReferenceId;
  final String? accessToken;
  final String? tokenType;
  final String? expiresAt;
  final PartyRecordDto? record;
  factory PartyAuthenticationOutputDto.fromJson(Map<String, dynamic> json) => PartyAuthenticationOutputDto(
    crReferenceId: json['crReferenceId']?.toString(),
    accessToken: json['accessToken']?.toString(),
    tokenType: json['tokenType']?.toString(),
    expiresAt: json['expiresAt']?.toString(),
    record: json['partyDataManagementInstanceRecord'] is Map
        ? PartyRecordDto.fromJson(Map<String, dynamic>.from(json['partyDataManagementInstanceRecord'] as Map))
        : null,
  );
}
