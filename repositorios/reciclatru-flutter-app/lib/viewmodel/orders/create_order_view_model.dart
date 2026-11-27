/// Coordina las acciones y el estado de create order view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../data/remote/network_exception.dart';
import '../../di/orders_module.dart';
import '../../di/profile_module.dart';
import '../../model/orders/order.dart';
import 'create_order_ui_state.dart';

/// Valida y publica pedidos, siguiendo las acciones de `CreateOrderViewModel.kt`.
final createOrderViewModelProvider =
    NotifierProvider.autoDispose<CreateOrderViewModel, CreateOrderUiState>(CreateOrderViewModel.new);

class CreateOrderViewModel extends Notifier<CreateOrderUiState> {
  int _nextItemId = 0;
  bool _loadingProfile = false;

  @override
  CreateOrderUiState build() => CreateOrderUiState(items: [OrderItemInput(id: _nextItemId++)]);

  /// Inicializa una visita nueva a la pantalla y consulta el perfil asociado.
  void onStart() {
    _nextItemId = 0;
    _loadingProfile = false;
    state = CreateOrderUiState(items: [OrderItemInput(id: _nextItemId++)]);
    loadProfile();
  }

  /// Reintenta leer la dirección si el perfil todavía no la entregó.
  void onResume() {
    if (state.pickupAddress == null) loadProfile();
  }

  /// Agrega un renglón hasta el máximo permitido por Kotlin.
  void onAddItem() {
    if (state.publishing || state.items.length >= 10) return;
    _updateItems((items) => [...items, OrderItemInput(id: _nextItemId++)]);
  }

  void onRemoveItem(int id) {
    if (state.publishing) return;
    _updateItems((items) => items.where((item) => item.id != id).toList());
  }

  void onDescriptionChange(int id, String value) => _updateItems((items) => items.map(
    (item) => item.id == id
        ? item.copyWith(description: value.length > 100 ? value.substring(0, 100) : value, clearError: true, descriptionInvalid: false)
        : item,
  ).toList());

  void onWeightChange(int id, String value) {
    if (!RegExp(r'^\d{0,3}([.,]\d?)?$').hasMatch(value)) return;
    _updateItems((items) => items.map(
      (item) => item.id == id
          ? item.copyWith(weight: value, clearError: true, weightInvalid: false)
          : item,
    ).toList());
  }

  void onPhotoAdded(String path) {
    if (state.publishing || state.photos.length >= 3) return;
    state = state.copyWith(photos: [...state.photos, path]);
  }

  void onPhotoRemoved(int index) {
    if (state.publishing || index < 0 || index >= state.photos.length) return;
    final photos = [...state.photos]..removeAt(index);
    state = state.copyWith(photos: photos);
  }

  void onProfileDialogDismissed() => state = state.copyWith(profileIncomplete: false);

  /// Valida materiales y sube fotos después de crear el pedido.
  Future<void> onPublish() async {
    if (state.publishing || state.published) return;
    if (state.items.isEmpty) {
      state = state.copyWith(itemsError: 'Agrega al menos un ítem para publicar', clearPublishError: true);
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
      state = state.copyWith(items: validated, clearPublishError: true);
      return;
    }

    final items = validated.map((item) => OrderItem(
      description: item.description.trim(),
      weightKg: (_toTenths(item.weight)! / 10),
    )).toList();
    state = state.copyWith(publishing: true, clearPublishError: true, clearItemsError: true);
    try {
      final order = await ref.read(createOrderUseCaseProvider).call(items);
      final photos = List<String>.from(state.photos);
      if (photos.isNotEmpty) {
        state = state.copyWith(uploadingPhotos: true);
        await ref.read(uploadOrderPhotosUseCaseProvider).call(order.id, photos);
      }
      state = state.copyWith(
        publishing: false,
        uploadingPhotos: false,
        published: true,
        publishedOrderId: order.id,
      );
    } catch (error) {
      if (error is NetworkException && error.code == 'PROFILE_INCOMPLETE') {
        state = state.copyWith(publishing: false, profileIncomplete: true);
      } else {
        final message = error is NetworkException &&
                (error.code == 'INVALID_ITEMS' || error.code == 'INVALID_WEIGHT')
            ? 'Revisa los ítems e inténtalo de nuevo.'
            : toUserMessage(error);
        state = state.copyWith(publishing: false, publishError: message);
      }
    }
  }

  /// Obtiene la dirección de recojo sin impedir publicar si falla la carga.
  Future<void> loadProfile() async {
    if (_loadingProfile) return;
    _loadingProfile = true;
    try {
      final profile = await ref.read(getProfileUseCaseProvider).call();
      final pickup = profile.pickupAddress;
      state = state.copyWith(
        pickupAddress: pickup == null
            ? null
            : [pickup.address, pickup.district].where((part) => part.trim().isNotEmpty).join(', '),
        clearPickupAddress: pickup == null,
        profileIncomplete: pickup == null,
      );
    } catch (_) {
      // Kotlin mantiene disponible el formulario aunque la consulta del perfil falle.
    } finally {
      _loadingProfile = false;
    }
  }

  /// Recalcula el peso total con precisión de décimas, igual que Kotlin.
  void _updateItems(List<OrderItemInput> Function(List<OrderItemInput>) transform) {
    final items = transform(state.items);
    final totalTenths = items.fold<int>(0, (sum, item) {
      final value = _toTenths(item.weight);
      return sum + (value != null && value >= 1 && value <= 5000 ? value : 0);
    });
    final label = totalTenths % 10 == 0
        ? '${totalTenths ~/ 10} kg'
        : '${totalTenths ~/ 10}.${totalTenths % 10} kg';
    state = state.copyWith(items: items, totalWeightLabel: label, clearItemsError: true, clearPublishError: true);
  }

  int? _toTenths(String input) {
    final normalized = input.replaceAll(',', '.');
    if (normalized.isEmpty || normalized == '.') return null;
    final pieces = normalized.split('.');
    final whole = int.tryParse(pieces.first.isEmpty ? '0' : pieces.first);
    final decimal = pieces.length == 1 ? 0 : int.tryParse(pieces[1].isEmpty ? '0' : pieces[1]);
    if (whole == null || decimal == null) return null;
    return whole * 10 + decimal;
  }
}
