class UserModel {
  final String id;
  final String email;
  final String role;
  final String? fullName;
  final String? phone;
  final String? reference;
  final bool profileComplete;
  final DateTime createdAt;

  UserModel({
    required this.id,
    required this.email,
    required this.role,
    this.fullName,
    this.phone,
    this.reference,
    required this.profileComplete,
    required this.createdAt,
  });

  factory UserModel.fromJson(Map<String, dynamic> json) {
    return UserModel(
      id: json['id'],
      email: json['email'],
      role: json['role'], 
      fullName: json['fullName'],
      phone: json['phone'],
      reference: json['reference'],
      profileComplete: json['profileComplete'] ?? false,
      createdAt: DateTime.parse(json['createdAt']),
    );
  }
}