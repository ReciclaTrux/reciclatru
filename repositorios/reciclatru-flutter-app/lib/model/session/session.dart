// Este modelo guarda los datos de la sesión iniciada por el usuario (su identificador, clave de acceso y rol).
enum UserRole {
  neighbor('NEIGHBOR'),
  recycler('RECYCLER');

  const UserRole(this.apiValue);
  final String apiValue;

  // Esta función convierte el rol enviado por el servidor al tipo de rol utilizado en la aplicación.
  static UserRole? fromApiValue(String? value) => switch (value) {
        'NEIGHBOR' => UserRole.neighbor,
        'RECYCLER' => UserRole.recycler,
        _ => null,
      };
}

// Esta clase contiene los datos de la sesión guardada en el dispositivo para mantener la cuenta abierta.
class Session {
  const Session({required this.userId, required this.token, required this.role});
  final String userId;
  final String token;
  final UserRole role;
}
