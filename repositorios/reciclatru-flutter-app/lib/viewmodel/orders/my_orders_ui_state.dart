/// Define los datos de interfaz de my orders y cómo se actualiza su estado.
import '../../model/orders/order_summary.dart';

/// Estado de una pestaña, con soporte para recarga y cursor.
class OrdersTabState {
  const OrdersTabState({
    this.loaded = false,
    this.loading = false,
    this.refreshing = false,
    this.loadingMore = false,
    this.orders = const [],
    this.nextCursor,
    this.error,
  });
  final bool loaded;
  final bool loading;
  final bool refreshing;
  final bool loadingMore;
  final List<OrderSummary> orders;
  final String? nextCursor;
  final String? error;

  OrdersTabState copyWith({
    bool? loaded,
    bool? loading,
    bool? refreshing,
    bool? loadingMore,
    List<OrderSummary>? orders,
    String? nextCursor,
    bool clearNextCursor = false,
    String? error,
    bool clearError = false,
  }) => OrdersTabState(
    loaded: loaded ?? this.loaded,
    loading: loading ?? this.loading,
    refreshing: refreshing ?? this.refreshing,
    loadingMore: loadingMore ?? this.loadingMore,
    orders: orders ?? this.orders,
    nextCursor: clearNextCursor ? null : nextCursor ?? this.nextCursor,
    error: clearError ? null : error ?? this.error,
  );
}

/// Estado de las pestañas Activos e Historial según `MyOrdersUiState.kt`.
class MyOrdersUiState {
  const MyOrdersUiState({
    this.selectedTab = OrdersTab.active,
    this.active = const OrdersTabState(),
    this.history = const OrdersTabState(),
    this.message,
    this.messageOrderId,
  });
  final OrdersTab selectedTab;
  final OrdersTabState active;
  final OrdersTabState history;
  final String? message;
  final String? messageOrderId;

  OrdersTabState tab(OrdersTab tab) => tab == OrdersTab.active ? active : history;

  MyOrdersUiState copyWith({
    OrdersTab? selectedTab,
    OrdersTabState? active,
    OrdersTabState? history,
    String? message,
    bool clearMessage = false,
    String? messageOrderId,
    bool clearMessageOrderId = false,
  }) => MyOrdersUiState(
    selectedTab: selectedTab ?? this.selectedTab,
    active: active ?? this.active,
    history: history ?? this.history,
    message: clearMessage ? null : message ?? this.message,
    messageOrderId: clearMessageOrderId ? null : messageOrderId ?? this.messageOrderId,
  );
}
