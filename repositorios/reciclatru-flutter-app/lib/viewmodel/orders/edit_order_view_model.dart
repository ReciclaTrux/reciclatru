/// Coordina las acciones y el estado de edit order view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/orders_module.dart';
import '../../model/orders/order.dart';
import 'create_order_ui_state.dart';
import 'edit_order_ui_state.dart';

/// Orquesta carga, validación y guardado incremental como Kotlin.
final editOrderViewModelProvider =
    NotifierProvider.autoDispose<EditOrderViewModel, EditOrderUiState>(EditOrderViewModel.new);

class EditOrderViewModel extends Notifier<EditOrderUiState> {
  String _orderId = '';
  int _nextItemId = 0;
  List<(String, String)> _initialItems = [];
  List<String> _removedPhotoIds = [];
  bool _itemsSaved = false;

  @override
  EditOrderUiState build() => const EditOrderUiState();

  /// Inicia el detalle identificado por la ruta actual.
  Future<void> onStart(String orderId) async {
    _orderId = orderId;
    _nextItemId = 0;
    _initialItems = [];
    _removedPhotoIds = [];
    _itemsSaved = false;
    state = const EditOrderUiState();
    await _load();
  }

  Future<void> onRetry() => _load();

  void onAddItem() {
    if (state.saving || state.items.length >= 10) return;
    _updateItems((items) => [...items, OrderItemInput(id: _nextItemId++)]);
  }

  void onRemoveItem(int id) {
    if (!state.saving) _updateItems((items) => items.where((item) => item.id != id).toList());
  }

  void onDescriptionChange(int id, String value) => _updateItems((items) => items.map((item) =>
    item.id == id ? item.copyWith(description: value.substring(0, value.length > 100 ? 100 : value.length), clearError: true) : item,
  ).toList());

  void onWeightChange(int id, String value) {
    if (!RegExp(r'^\d{0,3}([.,]\d?)?$').hasMatch(value)) return;
    _updateItems((items) => items.map((item) => item.id == id ? item.copyWith(weight: value, clearError: true) : item).toList());
  }

  void onPhotoAdded(String path) {
    if (state.saving || state.photoCount >= 3) return;
    state = state.copyWith(newPhotos: [...state.newPhotos, path]);
    _refreshDirty();
  }

  void onPhotoRemoved(int index) {
    if (state.saving || index < 0 || index >= state.photoCount) return;
    if (index < state.keptPhotos.length) {
      _removedPhotoIds = [..._removedPhotoIds, state.keptPhotos[index].id];
      state = state.copyWith(keptPhotos: state.keptPhotos.asMap().entries.where((entry) => entry.key != index).map((entry) => entry.value).toList());
    } else {
      final newIndex = index - state.keptPhotos.length;
      state = state.copyWith(newPhotos: state.newPhotos.asMap().entries.where((entry) => entry.key != newIndex).map((entry) => entry.value).toList());
    }
    _refreshDirty();
  }

  /// Devuelve `true` cuando la pantalla debe impedir el pop por guardado o diálogo.
  bool onBackRequested() {
    if (state.saving) return true;
    if (!state.dirty) return false;
    state = state.copyWith(discardDialogVisible: true);
    return true;
  }

  void onDiscardDismissed() => state = state.copyWith(discardDialogVisible: false);

  /// Persiste primero materiales, después eliminaciones y finalmente fotos nuevas.
  Future<void> onSave() async {
    if (state.saving || state.finished) return;
    if (!state.dirty) {
      state = state.copyWith(finished: true, clearFinishMessage: true);
      return;
    }
    if (state.items.isEmpty) {
      state = state.copyWith(itemsError: 'Agrega al menos un ítem para guardar', clearSaveError: true);
      return;
    }
    final validated = state.items.map((item) {
      final descriptionInvalid = item.description.trim().isEmpty;
      final tenths = _toTenths(item.weight);
      final weightInvalid = tenths == null || tenths < 1 || tenths > 5000;
      return item.copyWith(
        descriptionInvalid: descriptionInvalid,
        weightInvalid: weightInvalid,
        error: descriptionInvalid || weightInvalid ? 'Escribe el material y un peso mayor a 0 y hasta 500 kg' : null,
        clearError: !descriptionInvalid && !weightInvalid,
      );
    }).toList();
    if (validated.any((item) => item.error != null)) {
      state = state.copyWith(items: validated, clearSaveError: true);
      return;
    }
    final changed = _itemsChanged(validated);
    final domainItems = validated.map((item) => OrderItem(
      description: item.description.trim(),
      weightKg: _toTenths(item.weight)! / 10,
    )).toList();
    state = state.copyWith(saving: true, clearSaveError: true, clearItemsError: true);
    try {
      if (!_itemsSaved && changed) {
        await ref.read(updateOrderUseCaseProvider).call(_orderId, domainItems);
      }
      _itemsSaved = true;
      while (_removedPhotoIds.isNotEmpty) {
        await ref.read(removeOrderPhotoUseCaseProvider).call(_orderId, _removedPhotoIds.first);
        _removedPhotoIds = _removedPhotoIds.skip(1).toList();
      }
      if (state.newPhotos.isNotEmpty) {
        state = state.copyWith(uploadingPhotos: true);
        await ref.read(uploadOrderPhotosUseCaseProvider).call(_orderId, state.newPhotos);
      }
      state = state.copyWith(saving: false, uploadingPhotos: false, finished: true, finishMessage: 'Cambios guardados');
    } catch (error) {
      final network = error is NetworkException ? error : null;
      if (network?.code == 'ORDER_NOT_EDITABLE') {
        state = state.copyWith(saving: false, uploadingPhotos: false, finished: true, finishMessage: 'Este pedido ya no se puede editar');
      } else {
        state = state.copyWith(saving: false, uploadingPhotos: false, saveError: toUserMessage(error));
      }
    }
  }

  Future<void> _load() async {
    state = state.copyWith(loading: true, clearLoadError: true);
    try {
      final order = await ref.read(getOrderUseCaseProvider).call(_orderId);
      if (order == null || !order.canEdit) {
        state = state.copyWith(loading: false, finished: true, finishMessage: 'Este pedido ya no se puede editar');
        return;
      }
      _prefill(order);
    } catch (error) {
      state = state.copyWith(loading: false, loadError: toUserMessage(error));
    }
  }

  void _prefill(Order order) {
    final items = order.items.map((item) => OrderItemInput(
      id: _nextItemId++,
      description: item.description,
      weight: _weightText(item.weightKg),
    )).toList();
    _initialItems = items.map((item) => (item.description, item.weight)).toList();
    _removedPhotoIds = [];
    _itemsSaved = false;
    state = state.copyWith(
      loading: false,
      items: items,
      totalWeightLabel: _totalLabel(items),
      keptPhotos: order.photos,
      newPhotos: const [],
      dirty: false,
    );
  }

  void _updateItems(List<OrderItemInput> Function(List<OrderItemInput>) transform) {
    _itemsSaved = false;
    final items = transform(state.items);
    state = state.copyWith(items: items, totalWeightLabel: _totalLabel(items), clearItemsError: true, clearSaveError: true);
    _refreshDirty();
  }

  void _refreshDirty() => state = state.copyWith(
    dirty: _itemsChanged(state.items) || _removedPhotoIds.isNotEmpty || state.newPhotos.isNotEmpty,
  );

  bool _itemsChanged(List<OrderItemInput> items) {
    final current = items.map((item) => (item.description.trim(), _toTenths(item.weight)?.toString() ?? item.weight)).toList();
    final initial = _initialItems.map((item) => (item.$1.trim(), _toTenths(item.$2)?.toString() ?? item.$2)).toList();
    if (current.length != initial.length) return true;
    for (var index = 0; index < current.length; index++) {
      if (current[index] != initial[index]) return true;
    }
    return false;
  }

  String _totalLabel(List<OrderItemInput> items) {
    final total = items.fold<int>(0, (sum, item) {
      final value = _toTenths(item.weight);
      return sum + (value != null && value >= 1 && value <= 5000 ? value : 0);
    });
    return total % 10 == 0 ? '${total ~/ 10} kg' : '${total ~/ 10}.${total % 10} kg';
  }

  int? _toTenths(String value) {
    final parts = value.replaceAll(',', '.').split('.');
    if (value.isEmpty || value == '.') return null;
    final whole = int.tryParse(parts.first.isEmpty ? '0' : parts.first);
    final decimal = parts.length == 1 ? 0 : int.tryParse(parts[1].isEmpty ? '0' : parts[1]);
    return whole == null || decimal == null ? null : whole * 10 + decimal;
  }

  String _weightText(double value) {
    final tenths = (value * 10).round();
    return tenths % 10 == 0 ? '${tenths ~/ 10}' : '${tenths ~/ 10}.${tenths % 10}';
  }
}
