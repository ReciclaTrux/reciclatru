/// Define los datos de interfaz de order detail y cómo se actualiza su estado.
import '../../model/orders/order.dart';

/// Estado de la pantalla de detalle del vecino.
class OrderDetailUiState {
  const OrderDetailUiState({
    this.loading = true,
    this.refreshing = false,
    this.order,
    this.loadError,
    this.cancelDialogVisible = false,
    this.cancelling = false,
    this.acceptances = const [],
    this.acceptancesLoading = false,
    this.acceptancesError,
    this.recyclerToChoose,
    this.choosing = false,
    this.pendingPhotoCount = 0,
    this.uploadingPhotos = false,
    this.message,
  });
  final bool loading;
  final bool refreshing;
  final Order? order;
  final String? loadError;
  final bool cancelDialogVisible;
  final bool cancelling;
  final List<OrderAcceptance> acceptances;
  final bool acceptancesLoading;
  final String? acceptancesError;
  final OrderAcceptance? recyclerToChoose;
  final bool choosing;
  final int pendingPhotoCount;
  final bool uploadingPhotos;
  final String? message;

  OrderDetailUiState copyWith({
    bool? loading,
    bool? refreshing,
    Order? order,
    bool clearOrder = false,
    String? loadError,
    bool clearLoadError = false,
    bool? cancelDialogVisible,
    bool? cancelling,
    List<OrderAcceptance>? acceptances,
    bool? acceptancesLoading,
    String? acceptancesError,
    bool clearAcceptancesError = false,
    OrderAcceptance? recyclerToChoose,
    bool clearRecyclerToChoose = false,
    bool? choosing,
    int? pendingPhotoCount,
    bool? uploadingPhotos,
    String? message,
    bool clearMessage = false,
  }) => OrderDetailUiState(
    loading: loading ?? this.loading,
    refreshing: refreshing ?? this.refreshing,
    order: clearOrder ? null : order ?? this.order,
    loadError: clearLoadError ? null : loadError ?? this.loadError,
    cancelDialogVisible: cancelDialogVisible ?? this.cancelDialogVisible,
    cancelling: cancelling ?? this.cancelling,
    acceptances: acceptances ?? this.acceptances,
    acceptancesLoading: acceptancesLoading ?? this.acceptancesLoading,
    acceptancesError: clearAcceptancesError ? null : acceptancesError ?? this.acceptancesError,
    recyclerToChoose: clearRecyclerToChoose ? null : recyclerToChoose ?? this.recyclerToChoose,
    choosing: choosing ?? this.choosing,
    pendingPhotoCount: pendingPhotoCount ?? this.pendingPhotoCount,
    uploadingPhotos: uploadingPhotos ?? this.uploadingPhotos,
    message: clearMessage ? null : message ?? this.message,
  );
}
