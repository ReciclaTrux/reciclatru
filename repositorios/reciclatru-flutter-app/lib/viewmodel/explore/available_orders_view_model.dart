/// Coordina las acciones y el estado de available orders view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../di/explore_module.dart';
import '../../model/explore/available_order.dart';
import 'available_orders_ui_state.dart';

/// Estado del listado de pedidos publicados que el reciclador aún puede aceptar.
final availableOrdersViewModelProvider =
    NotifierProvider.autoDispose<AvailableOrdersViewModel, AvailableOrdersUiState>(AvailableOrdersViewModel.new);

/// Coordina permiso, consulta, refresco y paginación de pedidos disponibles.
class AvailableOrdersViewModel extends Notifier<AvailableOrdersUiState> {
  bool _started = false;
  bool _permissionGranted = false;
  bool _permanentlyDenied = false;
  GeoPoint? _lastLocation;

  @override
  AvailableOrdersUiState build() => const AvailableOrdersUiState();

  /// Muestra el aviso una sola vez o carga directamente si ya se mostró.
  Future<void> onStart(bool permissionGranted) async {
    if (_started) return;
    _started = true;
    _permissionGranted = permissionGranted;
    final promptWasShown = await ref.read(locationPromptStoreProvider).wasShown();
    if (!permissionGranted && !promptWasShown) {
      state = state.copyWith(showLocationPrompt: true);
    } else {
      await _load();
    }
  }

  /// Marca el aviso como mostrado; la vista solicita el permiso del sistema.
  Future<void> onPromptAccepted() async {
    await ref.read(locationPromptStoreProvider).markShown();
    state = state.copyWith(showLocationPrompt: false);
  }

  /// Marca el aviso como mostrado y carga sin coordenadas.
  Future<void> onPromptDismissed() async {
    await ref.read(locationPromptStoreProvider).markShown();
    state = state.copyWith(showLocationPrompt: false);
    await _load();
  }

  /// Recibe el resultado del permiso solicitado por el sistema operativo.
  Future<void> onPermissionResult(bool granted, bool permanentlyDenied) async {
    _permissionGranted = granted;
    _permanentlyDenied = permanentlyDenied;
    await _load();
  }

  /// Actualiza el listado al regresar de ajustes o reanudar la app.
  Future<void> onResume(bool permissionGranted) async {
    if (!_started || state.showLocationPrompt) return;
    if (permissionGranted != _permissionGranted) {
      _permissionGranted = permissionGranted;
      await _load();
    } else {
      await _refreshSilently();
    }
  }

  /// Reintenta la primera página tras un error inicial.
  Future<void> onRetry() => _load();

  /// Actualiza desde el inicio conservando el contenido visible ante un error.
  Future<void> onRefresh() async {
    if (state.loading || state.refreshing) return;
    state = state.copyWith(refreshing: true);
    try {
      await _fetchFirstPage();
      state = state.copyWith(refreshing: false);
    } catch (error) {
      state = state.copyWith(refreshing: false, message: _message(error));
    }
  }

  /// Carga la siguiente página indicada por el cursor de la API.
  Future<void> onLoadMore() async {
    final cursor = state.nextCursor;
    if (cursor == null || state.loading || state.refreshing || state.loadingMore) return;
    state = state.copyWith(loadingMore: true);
    try {
      final page = await ref.read(getAvailableOrdersUseCaseProvider)(_lastLocation, cursor: cursor);
      state = state.copyWith(
        loadingMore: false,
        orders: [...state.orders, ...page.items],
        nextCursor: page.nextCursor,
      );
    } catch (error) {
      state = state.copyWith(loadingMore: false, message: _message(error));
    }
  }

  /// Limpia el mensaje después de que la vista lo haya mostrado.
  void onMessageShown() => state = state.copyWith(message: null);

  Future<void> _load() async {
    if (state.loading) return;
    state = state.copyWith(loading: true, loadError: null);
    try {
      await _fetchFirstPage();
      state = state.copyWith(loading: false);
    } catch (error) {
      state = state.copyWith(loading: false, loadError: _message(error));
    }
  }

  Future<void> _refreshSilently() async {
    if (!state.loaded || state.loading || state.refreshing) return;
    try {
      await _fetchFirstPage();
    } catch (_) {
      // Se conserva la lista que ya estaba visible si falla la consulta silenciosa.
    }
  }

  /// Reemplaza las filas con la primera página y actualiza el aviso de ubicación.
  Future<void> _fetchFirstPage() async {
    final location = _permissionGranted
        ? await ref.read(deviceLocationProvider).currentLocation()
        : null;
    _lastLocation = location;
    final page = await ref.read(getAvailableOrdersUseCaseProvider)(location);
    state = state.copyWith(
      loaded: true,
      orders: page.items,
      nextCursor: page.nextCursor,
      sortedByDistance: page.sortedByDistance,
      locationNotice: !_permissionGranted
          ? (_permanentlyDenied ? LocationNotice.permissionDeniedPermanently : LocationNotice.permissionDenied)
          : location == null
              ? LocationNotice.unavailable
              : null,
    );
  }

  /// Convierte el error de red a texto que pueda mostrarse en la interfaz.
  String _message(Object error) => error.toString().replaceFirst('Exception: ', '');
}
