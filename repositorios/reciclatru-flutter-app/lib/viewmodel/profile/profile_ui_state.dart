/// Define los datos de interfaz de profile y cómo se actualiza su estado.
import '../../model/profile/user_profile.dart';

/// Datos que el perfil muestra y errores asociados a cada operación.
class ProfileUiState {
  const ProfileUiState({
    this.loading = true,
    this.loadError,
    this.email = '',
    this.role = '',
    this.savedFullName = '',
    this.fullName = '',
    this.phone = '',
    this.reference = '',
    this.pickupAddress,
    this.fullNameError,
    this.phoneError,
    this.saveError,
    this.saving = false,
    this.savedOnce = false,
    this.message,
  });

  final bool loading;
  final String? loadError;
  final String email;
  final String role;
  final String savedFullName;
  final String fullName;
  final String phone;
  final String reference;
  final PickupAddress? pickupAddress;
  final String? fullNameError;
  final String? phoneError;
  final String? saveError;
  final bool saving;
  final bool savedOnce;
  final String? message;

  /// Crea una copia del estado y conserva los valores omitidos.
  ProfileUiState copyWith({
    bool? loading,
    String? loadError,
    bool clearLoadError = false,
    String? email,
    String? role,
    String? savedFullName,
    String? fullName,
    String? phone,
    String? reference,
    PickupAddress? pickupAddress,
    bool clearPickupAddress = false,
    String? fullNameError,
    bool clearFullNameError = false,
    String? phoneError,
    bool clearPhoneError = false,
    String? saveError,
    bool clearSaveError = false,
    bool? saving,
    bool? savedOnce,
    String? message,
    bool clearMessage = false,
  }) => ProfileUiState(
    loading: loading ?? this.loading,
    loadError: clearLoadError ? null : loadError ?? this.loadError,
    email: email ?? this.email,
    role: role ?? this.role,
    savedFullName: savedFullName ?? this.savedFullName,
    fullName: fullName ?? this.fullName,
    phone: phone ?? this.phone,
    reference: reference ?? this.reference,
    pickupAddress: clearPickupAddress ? null : pickupAddress ?? this.pickupAddress,
    fullNameError: clearFullNameError ? null : fullNameError ?? this.fullNameError,
    phoneError: clearPhoneError ? null : phoneError ?? this.phoneError,
    saveError: clearSaveError ? null : saveError ?? this.saveError,
    saving: saving ?? this.saving,
    savedOnce: savedOnce ?? this.savedOnce,
    message: clearMessage ? null : message ?? this.message,
  );
}
