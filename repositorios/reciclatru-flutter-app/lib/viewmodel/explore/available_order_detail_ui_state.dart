/// Define los datos de interfaz de available order detail y cómo se actualiza su estado.
import '../../model/explore/available_order_detail.dart';

/// Estado de carga, detalle, confirmación y aceptación del reciclador.
class AvailableOrderDetailUiState {
  const AvailableOrderDetailUiState({
    this.loading = true,
    this.order,
    this.loadError,
    this.confirmDialogVisible = false,
    this.accepting = false,
    this.profileIncomplete = false,
    this.unavailable = false,
    this.message,
  });

  final bool loading;
  final AvailableOrderDetail? order;
  final String? loadError;
  final bool confirmDialogVisible;
  final bool accepting;
  final bool profileIncomplete;
  final bool unavailable;
  final String? message;

  AvailableOrderDetailUiState copyWith({
    bool? loading,
    Object? order = _unchanged,
    Object? loadError = _unchanged,
    bool? confirmDialogVisible,
    bool? accepting,
    bool? profileIncomplete,
    bool? unavailable,
    Object? message = _unchanged,
  }) => AvailableOrderDetailUiState(
    loading: loading ?? this.loading,
    order: identical(order, _unchanged) ? this.order : order as AvailableOrderDetail?,
    loadError: identical(loadError, _unchanged) ? this.loadError : loadError as String?,
    confirmDialogVisible: confirmDialogVisible ?? this.confirmDialogVisible,
    accepting: accepting ?? this.accepting,
    profileIncomplete: profileIncomplete ?? this.profileIncomplete,
    unavailable: unavailable ?? this.unavailable,
    message: identical(message, _unchanged) ? this.message : message as String?,
  );
}

const _unchanged = Object();
