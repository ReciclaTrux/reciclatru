/// Define los datos de interfaz de available orders y cómo se actualiza su estado.
import '../../model/explore/available_order.dart';

/// Situación de permisos que afecta cómo se ordenan los pedidos.
enum LocationNotice { permissionDenied, permissionDeniedPermanently, unavailable }

/// Estados de carga, lista, paginación y permiso de la pantalla de exploración.
class AvailableOrdersUiState {
  const AvailableOrdersUiState({
    this.showLocationPrompt = false,
    this.locationNotice,
    this.loaded = false,
    this.loading = false,
    this.refreshing = false,
    this.loadingMore = false,
    this.orders = const [],
    this.nextCursor,
    this.sortedByDistance = false,
    this.loadError,
    this.message,
  });

  final bool showLocationPrompt;
  final LocationNotice? locationNotice;
  final bool loaded;
  final bool loading;
  final bool refreshing;
  final bool loadingMore;
  final List<AvailableOrder> orders;
  final String? nextCursor;
  final bool sortedByDistance;
  final String? loadError;
  final String? message;

  AvailableOrdersUiState copyWith({
    bool? showLocationPrompt,
    Object? locationNotice = _unchanged,
    bool? loaded,
    bool? loading,
    bool? refreshing,
    bool? loadingMore,
    List<AvailableOrder>? orders,
    Object? nextCursor = _unchanged,
    bool? sortedByDistance,
    Object? loadError = _unchanged,
    Object? message = _unchanged,
  }) => AvailableOrdersUiState(
    showLocationPrompt: showLocationPrompt ?? this.showLocationPrompt,
    locationNotice: identical(locationNotice, _unchanged) ? this.locationNotice : locationNotice as LocationNotice?,
    loaded: loaded ?? this.loaded,
    loading: loading ?? this.loading,
    refreshing: refreshing ?? this.refreshing,
    loadingMore: loadingMore ?? this.loadingMore,
    orders: orders ?? this.orders,
    nextCursor: identical(nextCursor, _unchanged) ? this.nextCursor : nextCursor as String?,
    sortedByDistance: sortedByDistance ?? this.sortedByDistance,
    loadError: identical(loadError, _unchanged) ? this.loadError : loadError as String?,
    message: identical(message, _unchanged) ? this.message : message as String?,
  );
}

const _unchanged = Object();
