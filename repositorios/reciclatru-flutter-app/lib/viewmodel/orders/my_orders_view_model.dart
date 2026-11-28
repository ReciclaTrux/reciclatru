/// Coordina las acciones y el estado de my orders view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/orders_module.dart';
import '../../model/orders/order_summary.dart';
import 'my_orders_ui_state.dart';

/// ViewModel de pedidos propios con pestañas, actualización y paginación.
final myOrdersProvider = NotifierProvider.autoDispose<MyOrdersViewModel, MyOrdersUiState>(MyOrdersViewModel.new);

class MyOrdersViewModel extends Notifier<MyOrdersUiState> {
  @override
  MyOrdersUiState build() {
    Future.microtask(() => _load(OrdersTab.active));
    return const MyOrdersUiState();
  }

  void onTabSelected(OrdersTab tab) {
    state = state.copyWith(selectedTab: tab);
    if (state.tab(tab).loaded) {
      _refreshSilently(tab);
    } else {
      _load(tab);
    }
  }

  void onRetry() => _load(state.selectedTab);

  Future<void> onRefresh() async {
    final tab = state.selectedTab;
    final current = state.tab(tab);
    if (current.loading || current.refreshing) return;
    _updateTab(tab, (value) => value.copyWith(refreshing: true));
    try {
      final page = await ref.read(getMyOrdersUseCaseProvider).call(tab);
      _updateTab(tab, (value) => value.copyWith(
        loaded: true,
        refreshing: false,
        orders: page.items,
        nextCursor: page.nextCursor,
        clearNextCursor: page.nextCursor == null,
        clearError: true,
      ));
    } catch (error) {
      _updateTab(tab, (value) => value.copyWith(refreshing: false));
      state = state.copyWith(message: toUserMessage(error));
    }
  }

  void onResume() => _refreshSilently(state.selectedTab);

  Future<void> onLoadMore() async {
    final tab = state.selectedTab;
    final current = state.tab(tab);
    final cursor = current.nextCursor;
    if (cursor == null || current.loading || current.refreshing || current.loadingMore) return;
    _updateTab(tab, (value) => value.copyWith(loadingMore: true));
    try {
      final page = await ref.read(getMyOrdersUseCaseProvider).call(tab, cursor);
      _updateTab(tab, (value) => value.copyWith(
        loadingMore: false,
        orders: [...value.orders, ...page.items],
        nextCursor: page.nextCursor,
        clearNextCursor: page.nextCursor == null,
      ));
    } catch (error) {
      _updateTab(tab, (value) => value.copyWith(loadingMore: false));
      state = state.copyWith(message: toUserMessage(error));
    }
  }

  void onOrderPublished(String orderId) {
    final failures = ref.read(pendingPhotosStoreProvider).get(orderId).length;
    final message = failures == 0
        ? 'Pedido publicado'
        : failures == 1
            ? 'Pedido publicado, pero no se subió 1 foto'
            : 'Pedido publicado, pero no se subieron $failures fotos';
    state = state.copyWith(
      selectedTab: OrdersTab.active,
      message: message,
      messageOrderId: orderId.isEmpty ? null : orderId,
      clearMessageOrderId: orderId.isEmpty,
    );
    _load(OrdersTab.active);
  }

  void onMessageShown() => state = state.copyWith(clearMessage: true, clearMessageOrderId: true);

  Future<void> _load(OrdersTab tab) async {
    if (state.tab(tab).loading) return;
    _updateTab(tab, (value) => value.copyWith(loading: true, clearError: true));
    try {
      final page = await ref.read(getMyOrdersUseCaseProvider).call(tab);
      _updateTab(tab, (value) => value.copyWith(
        loaded: true,
        loading: false,
        orders: page.items,
        nextCursor: page.nextCursor,
        clearNextCursor: page.nextCursor == null,
      ));
    } catch (error) {
      _updateTab(tab, (value) => value.copyWith(loading: false, error: toUserMessage(error)));
    }
  }

  Future<void> _refreshSilently(OrdersTab tab) async {
    final current = state.tab(tab);
    if (!current.loaded || current.loading || current.refreshing) return;
    try {
      final page = await ref.read(getMyOrdersUseCaseProvider).call(tab);
      _updateTab(tab, (value) => value.copyWith(
        orders: page.items,
        nextCursor: page.nextCursor,
        clearNextCursor: page.nextCursor == null,
      ));
    } catch (_) {
      // La recarga de ciclo de vida no muestra alertas invasivas.
    }
  }

  void _updateTab(OrdersTab tab, OrdersTabState Function(OrdersTabState) transform) {
    state = tab == OrdersTab.active
        ? state.copyWith(active: transform(state.active))
        : state.copyWith(history: transform(state.history));
  }
}
