// Este archivo gestiona la información y las acciones del perfil del usuario (cargar datos, validar campos y guardar cambios).
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/profile_module.dart';
import '../../model/profile/user_profile.dart';
import 'profile_ui_state.dart';

// Este proveedor permite acceder a la lógica del perfil desde las pantallas de la app.
final profileViewModelProvider =
    NotifierProvider.autoDispose<ProfileViewModel, ProfileUiState>(ProfileViewModel.new);

// Esta clase maneja la carga, edición y guardado de los datos del perfil de la persona.
class ProfileViewModel extends Notifier<ProfileUiState> {
  @override
  ProfileUiState build() {
    // Al abrir la pantalla, inicia la carga automática del perfil.
    Future<void>.microtask(load);
    return const ProfileUiState();
  }

  // Esta función vuelve a intentar cargar la información si ocurrió un error previo.
  Future<void> onRetry() => load();

  // Esta función actualiza el nombre y apellido limitando el texto a un máximo de 80 caracteres.
  void onFullNameChange(String value) {
    state = state.copyWith(
      fullName: value.length > 80 ? value.substring(0, 80) : value,
      clearFullNameError: true,
      clearSaveError: true,
    );
  }

  // Esta función guarda solo los números del celular y limita la longitud a exactamente 9 dígitos.
  void onPhoneChange(String value) {
    final digits = value.replaceAll(RegExp(r'\D'), '');
    state = state.copyWith(
      phone: digits.length > 9 ? digits.substring(0, 9) : digits,
      clearPhoneError: true,
      clearSaveError: true,
    );
  }

  // Esta función actualiza la referencia de la dirección limitando el texto a un máximo de 150 caracteres.
  void onReferenceChange(String value) {
    state = state.copyWith(
      reference: value.length > 150 ? value.substring(0, 150) : value,
      clearSaveError: true,
    );
  }

  // Esta función actualiza el punto de recojo cuando el vecino regresa de la pantalla del mapa.
  Future<void> onReturnFromPickupPoint() async {
    if (state.loading || state.loadError != null) return;
    try {
      final profile = await ref.read(getProfileUseCaseProvider).call();
      state = state.copyWith(
        pickupAddress: profile.pickupAddress,
        clearPickupAddress: profile.pickupAddress == null,
      );
    } catch (_) {
      // Si falla la actualización en segundo plano, se conserva la información que ya estaba en pantalla.
    }
  }

  // Esta función oculta el mensaje de confirmación una vez mostrado.
  void onMessageShown() => state = state.copyWith(clearMessage: true);

  // Esta función valida que el nombre tenga al menos 2 letras y el celular tenga 9 dígitos iniciando con 9, y luego guarda el perfil.
  Future<void> onSave() async {
    if (state.saving || state.loading) return;
    final fullName = state.fullName.trim();
    final nameError = fullName.length < 2 ? 'Ingresa tu nombre y apellido.' : null;
    final phoneError = RegExp(r'^9\d{8}$').hasMatch(state.phone)
        ? null
        : 'Debe tener 9 dígitos y empezar con 9';
    state = state.copyWith(
      fullNameError: nameError,
      phoneError: phoneError,
      clearFullNameError: nameError == null,
      clearPhoneError: phoneError == null,
      clearSaveError: true,
    );
    if (nameError != null || phoneError != null) return;

    state = state.copyWith(saving: true, clearSaveError: true);
    try {
      final updated = await ref.read(updateProfileUseCaseProvider).call(
        fullName: fullName,
        phone: state.phone,
        reference: state.role == 'NEIGHBOR' ? state.reference : null,
      );
      state = _withProfile(updated).copyWith(
        saving: false,
        savedOnce: true,
        message: 'Guardamos tu perfil.',
      );
    } catch (error) {
      final network = error is NetworkException ? error : null;
      final saveError = network?.statusCode == 400
          ? 'Revisa tus datos e inténtalo de nuevo.'
          : toUserMessage(error);
      state = state.copyWith(saving: false, saveError: saveError);
    }
  }

  // Esta función consulta los datos del perfil al servidor para mostrarlos en la pantalla.
  Future<void> load() async {
    state = state.copyWith(loading: true, clearLoadError: true);
    try {
      final profile = await ref.read(getProfileUseCaseProvider).call();
      state = _withProfile(profile).copyWith(loading: false);
    } catch (error) {
      state = state.copyWith(loading: false, loadError: toUserMessage(error));
    }
  }

  // Esta función pasa la información del perfil recibida del servidor al estado que lee la pantalla.
  ProfileUiState _withProfile(UserProfile profile) => state.copyWith(
    email: profile.email,
    role: profile.role,
    savedFullName: profile.fullName ?? '',
    fullName: profile.fullName ?? '',
    phone: profile.phone ?? '',
    reference: profile.reference ?? '',
    pickupAddress: profile.pickupAddress,
    clearPickupAddress: profile.pickupAddress == null,
  );
}
