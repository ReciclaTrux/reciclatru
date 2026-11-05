import 'user_model.dart';

class AuthResponse {
  final String crReferenceId;
  final String accessToken;
  final String tokenType;
  final DateTime expiresAt;
  final UserModel user;

  AuthResponse({
    required this.crReferenceId,
    required this.accessToken,
    required this.tokenType,
    required this.expiresAt,
    required this.user,
  });

  factory AuthResponse.fromJson(Map<String, dynamic> json) {
    return AuthResponse(
      crReferenceId: json['crReferenceId'],
      accessToken: json['accessToken'],
      tokenType: json['tokenType'] ?? 'Bearer',
      expiresAt: DateTime.parse(json['expiresAt']),
      user: UserModel.fromJson(json['partyDataManagementInstanceRecord']),
    );
  }
}