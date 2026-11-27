// Este archivo controla el proceso de búsqueda, selección en el mapa y guardado de la dirección de recojo.
import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/profile_module.dart';
import '../../domain/repository/profile/geocoding_repository.dart';
import '../../domain/usecase/profile/describe_point_use_case.dart';
import '../../domain/usecase/profile/search_address_use_case.dart';
import '../../domain/usecase/profile/suggest_addresses_use_case.dart';
import '../../model/profile/geo_point.dart';
import '../../model/profile/pickup_location.dart';
import 'location_ui_state.dart';

// Este proveedor permite acceder a la lógica de selección de ubicación desde la pantalla correspondiente.
final locationViewModelProvider =
    NotifierProvider<LocationViewModel, LocationUiState>(LocationViewModel.new);

// Esta clase maneja las acciones de buscar direcciones, mover el mapa y guardar el punto de recojo.
class LocationViewModel extends Notifier<LocationUiState> {
  Timer? _debounce;
  int _requestId = 0;

  @override
  LocationUiState build() {
    ref.onDispose(() => _debounce?.cancel());
    return const LocationUiState();
  }

  // Esta función verifica si el servicio de búsqueda de direcciones en el mapa está disponible en el teléfono.
  Future<void> onStart() async {
    state = const LocationUiState(geocodingAvailable: false);
    final repository = ref.read(geocodingRepositoryProvider);
    await repository.initialize();
    state = state.copyWith(geocodingAvailable: repository.isAvailable);
  }

  // Esta función busca sugerencias de direcciones mientras la persona escribe, esperando 300 milisegundos tras la última tecla.
  void onSearchQueryChanged(String query) {
    _debounce?.cancel();
    final id = ++_requestId;
    if (query.trim().isEmpty) {
      state = state.copyWith(searching: false, suggestions: const [], searchError: null);
      return;
    }
    state = state.copyWith(searching: true, suggestions: const [], searchError: null);
    _debounce = Timer(const Duration(milliseconds: 300), () => _suggest(query, id));
  }

  // Esta función busca la dirección exacta ingresada en el buscador y ubica el mapa en las coordenadas encontradas.
  Future<void> onSearch(String query) async {
    _debounce?.cancel();
    final id = ++_requestId;
    if (query.trim().isEmpty) {
      state = state.copyWith(searching: false, suggestions: const [], searchError: null);
      return;
    }
    state = state.copyWith(searching: true, suggestions: const [], searchError: null);
    try {
      final point = await ref.read(searchAddressUseCaseProvider).call(query);
      if (id != _requestId) return;
      state = state.copyWith(
        searching: false,
        center: point ?? state.center,
        searchError: point == null ? 'No encontramos esa dirección. Mueve el mapa a mano.' : null,
      );
    } catch (_) {
      if (id != _requestId) return;
      state = state.copyWith(
        searching: false,
        searchError: 'No pudimos buscar la dirección. Mueve el mapa a mano.',
      );
    }
  }

  // Esta función obtiene la lista de opciones de direcciones sugeridas.
  Future<void> _suggest(String query, int id) async {
    try {
      final suggestions = await ref.read(suggestAddressesUseCaseProvider).call(query);
      if (id != _requestId) return;
      state = state.copyWith(
        searching: false,
        suggestions: suggestions,
        searchError: suggestions.isEmpty ? 'No encontramos esa dirección. Mueve el mapa a mano.' : null,
      );
    } catch (_) {
      if (id != _requestId) return;
      state = state.copyWith(
        searching: false,
        suggestions: const [],
        searchError: 'No pudimos buscar la dirección. Mueve el mapa a mano.',
      );
    }
  }

  // Esta función centra el mapa en la sugerencia elegida por la persona.
  void onSuggestionSelected(AddressSuggestion suggestion) {
    _debounce?.cancel();
    _requestId++;
    state = state.copyWith(
      center: suggestion.point,
      searching: false,
      suggestions: const [],
      searchError: null,
    );
  }

  // Esta función toma el punto marcado en el mapa e intenta obtener el nombre de la calle y el distrito automáticamente.
  Future<void> onMapConfirmed(double latitude, double longitude) async {
    _debounce?.cancel();
    _requestId++;
    final point = GeoPoint(
      latitude: double.parse(latitude.toStringAsFixed(6)),
      longitude: double.parse(longitude.toStringAsFixed(6)),
    );
    state = state.copyWith(
      step: LocationStep.address,
      center: point,
      address: '',
      district: '',
      resolvingAddress: true,
      searching: false,
      suggestions: const [],
      searchError: null,
      addressError: null,
      districtError: null,
      saveError: null,
      savedLocation: null,
    );
    try {
      final description = await ref.read(describePointUseCaseProvider).call(point);
      if (state.step != LocationStep.address || state.center != point) return;
      if (description == null) {
        state = state.copyWith(
          resolvingAddress: false,
          addressError: 'No pudimos leer la dirección. Escríbela tú.',
        );
      } else {
        state = state.copyWith(
          resolvingAddress: false,
          address: description.address,
          district: description.district,
        );
      }
    } catch (_) {
      if (state.step != LocationStep.address || state.center != point) return;
      state = state.copyWith(
        resolvingAddress: false,
        addressError: 'No pudimos leer la dirección. Escríbela tú.',
      );
    }
  }

  // Esta función guarda la dirección escrita manualmente y borra los mensajes de error asociados.
  void onAddressChange(String value) => state = state.copyWith(
        address: value,
        addressError: null,
        saveError: null,
      );

  // Esta función guarda el distrito escrito manualmente y borra los mensajes de error asociados.
  void onDistrictChange(String value) => state = state.copyWith(
        district: value,
        districtError: null,
        saveError: null,
      );

  // Esta función comprueba que la dirección y el distrito no estén vacíos antes de enviarlos al servidor.
  Future<void> onSave() async {
    if (state.saving) return;
    final point = state.center;
    final address = state.address.trim();
    final district = state.district.trim();
    final addressError = address.isEmpty ? 'Escribe tu dirección.' : null;
    final districtError = district.isEmpty ? 'Escribe tu distrito.' : null;
    if (addressError != null || districtError != null) {
      state = state.copyWith(addressError: addressError, districtError: districtError);
      return;
    }
    if (point == null) {
      state = state.copyWith(saveError: 'Selecciona un punto en el mapa.');
      return;
    }
    state = state.copyWith(saving: true, saveError: null);
    try {
      final saved = await ref.read(savePickupLocationUseCaseProvider).call(PickupLocation(
        point: point,
        address: address,
        district: district,
      ));
      final savedAddress = PickupAddress(
        latitude: saved.point.latitude,
        longitude: saved.point.longitude,
        address: saved.address,
        district: saved.district,
      );
      state = state.copyWith(
        step: LocationStep.saved,
        saving: false,
        address: saved.address,
        district: saved.district,
        savedLocation: savedAddress,
      );
    } catch (error) {
      state = state.copyWith(
        saving: false,
        saveError: toUserMessage(
          error,
          fallback: 'No pudimos guardar tu ubicación. Inténtalo de nuevo.',
        ),
      );
    }
  }

  // Esta función permite regresar al mapa para volver a mover el marcador sin perder la selección previa.
  void onBackToMap() => state = state.copyWith(
        step: LocationStep.map,
        resolvingAddress: false,
        addressError: null,
        districtError: null,
        saveError: null,
      );
}
