/// Define los datos de interfaz de edit order y cómo se actualiza su estado.
import '../../model/orders/order.dart';
import 'create_order_ui_state.dart';

/// Estado de edición de materiales y fotos de un pedido pendiente.
class EditOrderUiState {
  const EditOrderUiState({
    this.loading = true,
    this.loadError,
    this.items = const [],
    this.totalWeightLabel = '0 kg',
    this.keptPhotos = const [],
    this.newPhotos = const [],
    this.dirty = false,
    this.itemsError,
    this.saveError,
    this.saving = false,
    this.uploadingPhotos = false,
    this.discardDialogVisible = false,
    this.finished = false,
    this.finishMessage,
  });
  final bool loading;
  final String? loadError;
  final List<OrderItemInput> items;
  final String totalWeightLabel;
  final List<OrderPhoto> keptPhotos;
  final List<String> newPhotos;
  final bool dirty;
  final String? itemsError;
  final String? saveError;
  final bool saving;
  final bool uploadingPhotos;
  final bool discardDialogVisible;
  final bool finished;
  final String? finishMessage;
  int get photoCount => keptPhotos.length + newPhotos.length;

  EditOrderUiState copyWith({
    bool? loading,
    String? loadError,
    bool clearLoadError = false,
    List<OrderItemInput>? items,
    String? totalWeightLabel,
    List<OrderPhoto>? keptPhotos,
    List<String>? newPhotos,
    bool? dirty,
    String? itemsError,
    bool clearItemsError = false,
    String? saveError,
    bool clearSaveError = false,
    bool? saving,
    bool? uploadingPhotos,
    bool? discardDialogVisible,
    bool? finished,
    String? finishMessage,
    bool clearFinishMessage = false,
  }) => EditOrderUiState(
    loading: loading ?? this.loading,
    loadError: clearLoadError ? null : loadError ?? this.loadError,
    items: items ?? this.items,
    totalWeightLabel: totalWeightLabel ?? this.totalWeightLabel,
    keptPhotos: keptPhotos ?? this.keptPhotos,
    newPhotos: newPhotos ?? this.newPhotos,
    dirty: dirty ?? this.dirty,
    itemsError: clearItemsError ? null : itemsError ?? this.itemsError,
    saveError: clearSaveError ? null : saveError ?? this.saveError,
    saving: saving ?? this.saving,
    uploadingPhotos: uploadingPhotos ?? this.uploadingPhotos,
    discardDialogVisible: discardDialogVisible ?? this.discardDialogVisible,
    finished: finished ?? this.finished,
    finishMessage: clearFinishMessage ? null : finishMessage ?? this.finishMessage,
  );
}
