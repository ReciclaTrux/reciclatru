/// Coordina las acciones y el estado de available order detail view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../di/explore_module.dart';
import '../../domain/usecase/explore/accept_order_use_case.dart';
import '../../model/explore/available_order.dart';
import '../../data/remote/network_exception.dart';
import 'available_order_detail_ui_state.dart';

/// Provider del estado del detalle de pedido del reciclador.
final availableOrderDetailViewModelProvider =
    NotifierProvider.autoDispose<AvailableOrderDetailViewModel, AvailableOrderDetailUiState>(AvailableOrderDetailViewModel.new);

/// Carga el detalle con ubicación opcional y coordina la aceptación del pedido.
class AvailableOrderDetailViewModel extends Notifier<AvailableOrderDetailUiState> {
  String _orderId = '';
  GeoPoint? _location;

  @override
  AvailableOrderDetailUiState build() => const AvailableOrderDetailUiState();

  /// Inicia el detalle de la ruta solicitada.
  Future<void> onStart(String orderId) async {
    _orderId = orderId;
    _location = null;
    state = const AvailableOrderDetailUiState();
    await _load();
  }

  /// Reintenta cargar el detalle después de un error recuperable.
  Future<void> onRetry() => _load();

  /// Abre la confirmación solo si todavía se puede aceptar el pedido.
  void onAcceptClick() {
    final order = state.order;
    if (state.accepting || state.confirmDialogVisible || order == null || order.alreadyAccepted) return;
    state = state.copyWith(confirmDialogVisible: true);
  }

  /// Cierra el diálogo de confirmación.
  void onAcceptDismissed() {
    if (!state.accepting) state = state.copyWith(confirmDialogVisible: false);
  }

  /// Envía la aceptación y conserva el detalle que devuelve la API.
  Future<void> onAcceptConfirmed() async {
    if (state.accepting) return;
    state = state.copyWith(accepting: true, message: null);
    try {
      final order = await ref.read(acceptOrderUseCaseProvider)(_orderId);
      state = state.copyWith(accepting: false, confirmDialogVisible: false, order: order);
    } catch (error) {
      final code = error is NetworkException ? error.code : null;
      if (code == 'ORDER_NOT_AVAILABLE') {
        state = state.copyWith(accepting: false, confirmDialogVisible: false, unavailable: true);
      } else if (code == 'PROFILE_INCOMPLETE') {
        state = state.copyWith(accepting: false, confirmDialogVisible: false, profileIncomplete: true);
      } else {
        state = state.copyWith(
          accepting: false,
          confirmDialogVisible: false,
          message: toUserMessage(error),
        );
      }
    }
  }

  /// Cierra el aviso que explica que falta completar el perfil.
  void onProfileDialogDismissed() => state = state.copyWith(profileIncomplete: false);

  /// Limpia el mensaje después de que la vista lo presenta.
  void onMessageShown() => state = state.copyWith(message: null);

  /// Carga el punto actual solo para calcular la distancia del detalle.
  Future<void> _load() async {
    state = state.copyWith(loading: true, loadError: null, unavailable: false);
    try {
      final currentPoint = await ref.read(deviceLocationProvider).currentLocation();
      _location = currentPoint;
      final order = await ref.read(getAvailableOrderDetailUseCaseProvider)(_orderId, _location);
      state = state.copyWith(loading: false, order: order);
    } catch (error) {
      if (error is NetworkException && error.code == 'ORDER_NOT_AVAILABLE') {
        state = state.copyWith(loading: false, unavailable: true);
      } else {
        state = state.copyWith(loading: false, loadError: toUserMessage(error));
      }
    }
  }
}
