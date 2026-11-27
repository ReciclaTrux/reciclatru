/// Define los datos de interfaz de location y cómo se actualiza su estado.
import '../../domain/repository/profile/geocoding_repository.dart';
import '../../model/profile/geo_point.dart';
import '../../model/profile/user_profile.dart';

/// Pasos internos de la única pantalla de ubicación de Kotlin.
enum LocationStep { map, address, saved }

/// Estado visible y editable del flujo de ubicación.
class LocationUiState {
  const LocationUiState({
    this.step = LocationStep.map,
    this.initialCenter = const GeoPoint(latitude: -8.1116, longitude: -79.0287),
    this.center = const GeoPoint(latitude: -8.1116, longitude: -79.0287),
    this.geocodingAvailable = true,
    this.searching = false,
    this.suggestions = const [],
    this.searchError,
    this.resolvingAddress = false,
    this.address = '',
    this.district = '',
    this.addressError,
    this.districtError,
    this.saving = false,
    this.saveError,
    this.savedLocation,
  });

  /// Paso que la pantalla está mostrando.
  final LocationStep step;
  /// Centro original del mapa para el primer render.
  final GeoPoint initialCenter;
  /// Punto seleccionado en el mapa.
  final GeoPoint? center;
  /// Indica si el dispositivo tiene servicio de geocodificación disponible.
  final bool geocodingAvailable;
  /// Indicador mientras se obtienen sugerencias.
  final bool searching;
  /// Sugerencias de direcciones devueltas por la búsqueda.
  final List<AddressSuggestion> suggestions;
  /// Mensaje de error de búsqueda.
  final String? searchError;
  /// Indicador de resolución de dirección del punto.
  final bool resolvingAddress;
  /// Dirección que se puede corregir antes de guardar.
  final String address;
  /// Distrito que se puede corregir antes de guardar.
  final String district;
  /// Errores de los campos de dirección.
  final String? addressError;
  final String? districtError;
  /// Indicador de guardado remoto.
  final bool saving;
  /// Error devuelto al guardar.
  final String? saveError;
  /// Ubicación confirmada por la API.
  final PickupAddress? savedLocation;

  /// Copia el estado y permite limpiar errores anulables de forma explícita.
  LocationUiState copyWith({
    LocationStep? step,
    GeoPoint? initialCenter,
    GeoPoint? center,
    bool? geocodingAvailable,
    bool? searching,
    List<AddressSuggestion>? suggestions,
    Object? searchError = _unchanged,
    bool? resolvingAddress,
    String? address,
    String? district,
    Object? addressError = _unchanged,
    Object? districtError = _unchanged,
    bool? saving,
    Object? saveError = _unchanged,
    Object? savedLocation = _unchanged,
  }) => LocationUiState(
    step: step ?? this.step,
    initialCenter: initialCenter ?? this.initialCenter,
    center: center ?? this.center,
    geocodingAvailable: geocodingAvailable ?? this.geocodingAvailable,
    searching: searching ?? this.searching,
    suggestions: suggestions ?? this.suggestions,
    searchError: identical(searchError, _unchanged) ? this.searchError : searchError as String?,
    resolvingAddress: resolvingAddress ?? this.resolvingAddress,
    address: address ?? this.address,
    district: district ?? this.district,
    addressError: identical(addressError, _unchanged) ? this.addressError : addressError as String?,
    districtError: identical(districtError, _unchanged) ? this.districtError : districtError as String?,
    saving: saving ?? this.saving,
    saveError: identical(saveError, _unchanged) ? this.saveError : saveError as String?,
    savedLocation: identical(savedLocation, _unchanged) ? this.savedLocation : savedLocation as PickupAddress?,
  );
}

/// Marcador privado para diferenciar "no cambiar" de "limpiar el valor".
const _unchanged = Object();
