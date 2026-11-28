/// Coordina las acciones y el estado de order detail view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/orders_module.dart';
import '../../model/orders/order.dart';
import '../../model/orders/order_acceptance.dart';
import '../../model/orders/order_summary.dart';
import 'order_detail_ui_state.dart';

/// Coordina detalle, aceptaciones, selección de reciclador y cancelación.
final orderDetailViewModelProvider = NotifierProvider.autoDispose<OrderDetailViewModel, OrderDetailUiState>(OrderDetailViewModel.new);

class OrderDetailViewModel extends Notifier<OrderDetailUiState> {
  String _orderId = '';

  @override
  OrderDetailUiState build() => const OrderDetailUiState();

  /// Carga un destino cuando la navegación entrega el identificador.
  Future<void> onStart(String orderId) async {
    _orderId = orderId;
    state = const OrderDetailUiState();
    await _load();
  }

  Future<void> onRetry() => _load();

  Future<void> onRefresh() async {
    if (state.loading || state.refreshing) return;
    state = state.copyWith(refreshing: true);
    try {
      final order = await ref.read(getOrderUseCaseProvider).call(_orderId);
      if (order != null) {
        state = state.copyWith(order: order);
        await _refreshAcceptances(order);
      }
      state = state.copyWith(refreshing: false);
    } catch (error) {
      state = state.copyWith(refreshing: false, message: toUserMessage(error));
    }
  }

  void onCancelClick() {
    if (state.order?.canCancel == true) state = state.copyWith(cancelDialogVisible: true);
  }

  void onCancelDismissed() {
    if (!state.cancelling) state = state.copyWith(cancelDialogVisible: false);
  }

  Future<void> onCancelConfirmed() async {
    if (state.cancelling) return;
    state = state.copyWith(cancelling: true);
    try {
      final order = await ref.read(cancelOrderUseCaseProvider).call(_orderId);
      state = state.copyWith(cancelling: false, cancelDialogVisible: false, order: order, message: 'Pedido cancelado');
    } catch (error) {
      final notCancellable = error is NetworkException && error.code == 'ORDER_NOT_CANCELLABLE';
      state = state.copyWith(
        cancelling: false,
        cancelDialogVisible: false,
        message: notCancellable ? 'Este pedido ya no se puede cancelar.' : toUserMessage(error),
      );
      if (notCancellable) await onRefresh();
    }
  }

  Future<void> onRetryAcceptances() async {
    final order = state.order;
    if (order == null || state.acceptancesLoading) return;
    await _refreshAcceptances(order);
  }

  void onChooseClick(OrderAcceptance acceptance) {
    if (state.choosing || state.recyclerToChoose != null) return;
    if (state.order?.status != OrderStatus.pending || acceptance.status != AcceptanceStatus.accepted) return;
    state = state.copyWith(recyclerToChoose: acceptance);
  }

  void onChooseDismissed() {
    if (!state.choosing) state = state.copyWith(clearRecyclerToChoose: true);
  }

  Future<void> onChooseConfirmed() async {
    final target = state.recyclerToChoose;
    if (target == null || state.choosing) return;
    state = state.copyWith(choosing: true);
    try {
      final order = await ref.read(chooseRecyclerUseCaseProvider).call(_orderId, target.id);
      state = state.copyWith(
        choosing: false,
        clearRecyclerToChoose: true,
        order: order,
        acceptances: const [],
        clearAcceptancesError: true,
        message: 'Elegiste a ${target.fullName.split(' ').first}',
      );
    } catch (error) {
      final notPending = error is NetworkException && error.code == 'ORDER_NOT_PENDING';
      state = state.copyWith(
        choosing: false,
        clearRecyclerToChoose: true,
        message: notPending ? 'Este pedido ya no está pendiente.' : toUserMessage(error),
      );
      if (notPending) await onRefresh();
    }
  }

  Future<void> onRetryPhotos() async {
    final store = ref.read(pendingPhotosStoreProvider);
    final photos = store.get(_orderId);
    if (state.uploadingPhotos || photos.isEmpty) return;
    state = state.copyWith(uploadingPhotos: true);
    final failed = await ref.read(uploadOrderPhotosUseCaseProvider).call(_orderId, photos);
    Order? order = state.order;
    try {
      order = await ref.read(getOrderUseCaseProvider).call(_orderId);
    } catch (_) {
      // Se conserva el detalle cargado si falla la actualización de la respuesta.
    }
    state = state.copyWith(
      uploadingPhotos: false,
      pendingPhotoCount: failed,
      order: order,
      message: failed == 0 ? 'Fotos subidas' : 'No pudimos subir las fotos. Inténtalo de nuevo.',
    );
  }

  void onMessageShown() => state = state.copyWith(clearMessage: true);

  Future<void> _refreshAcceptances(Order order) async {
    if (order.status != OrderStatus.pending) {
      state = state.copyWith(acceptances: const [], clearAcceptancesError: true);
      return;
    }
    state = state.copyWith(acceptancesLoading: state.acceptances.isEmpty);
    try {
      final rows = await ref.read(getOrderAcceptancesUseCaseProvider).call(_orderId);
      state = state.copyWith(acceptances: rows, clearAcceptancesError: true);
    } catch (error) {
      final message = toUserMessage(error);
      if (state.acceptances.isEmpty) {
        state = state.copyWith(acceptancesError: message);
      } else {
        state = state.copyWith(message: message);
      }
    } finally {
      state = state.copyWith(acceptancesLoading: false);
    }
  }

  Future<void> _load() async {
    state = state.copyWith(loading: true, clearLoadError: true);
    try {
      final order = await ref.read(getOrderUseCaseProvider).call(_orderId);
      state = state.copyWith(
        loading: false,
        order: order,
        pendingPhotoCount: ref.read(pendingPhotosStoreProvider).get(_orderId).length,
      );
      if (order != null) await _refreshAcceptances(order);
    } catch (error) {
      state = state.copyWith(loading: false, loadError: toUserMessage(error));
    }
  }
}
