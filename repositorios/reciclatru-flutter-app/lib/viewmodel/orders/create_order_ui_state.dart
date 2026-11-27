/// Campos editables de cada material del formulario Kotlin.
class OrderItemInput {
  const OrderItemInput({
    required this.id,
    this.description = '',
    this.weight = '',
    this.error,
    this.descriptionInvalid = false,
    this.weightInvalid = false,
  });
  final int id;
  final String description;
  final String weight;
  final String? error;
  final bool descriptionInvalid;
  final bool weightInvalid;

  OrderItemInput copyWith({
    String? description,
    String? weight,
    String? error,
    bool clearError = false,
    bool? descriptionInvalid,
    bool? weightInvalid,
  }) => OrderItemInput(
    id: id,
    description: description ?? this.description,
    weight: weight ?? this.weight,
    error: clearError ? null : error ?? this.error,
    descriptionInvalid: descriptionInvalid ?? this.descriptionInvalid,
    weightInvalid: weightInvalid ?? this.weightInvalid,
  );
}

/// Estado de publicación migrado desde `CreateOrderUiState.kt`.
class CreateOrderUiState {
  const CreateOrderUiState({
    this.items = const [],
    this.totalWeightLabel = '0 kg',
    this.photos = const [],
    this.uploadingPhotos = false,
    this.pickupAddress,
    this.profileIncomplete = false,
    this.itemsError,
    this.publishError,
    this.publishing = false,
    this.published = false,
    this.publishedOrderId,
  });
  final List<OrderItemInput> items;
  final String totalWeightLabel;
  final List<String> photos;
  final bool uploadingPhotos;
  final String? pickupAddress;
  final bool profileIncomplete;
  final String? itemsError;
  final String? publishError;
  final bool publishing;
  final bool published;
  final String? publishedOrderId;

  CreateOrderUiState copyWith({
    List<OrderItemInput>? items,
    String? totalWeightLabel,
    List<String>? photos,
    bool? uploadingPhotos,
    String? pickupAddress,
    bool clearPickupAddress = false,
    bool? profileIncomplete,
    String? itemsError,
    bool clearItemsError = false,
    String? publishError,
    bool clearPublishError = false,
    bool? publishing,
    bool? published,
    String? publishedOrderId,
  }) => CreateOrderUiState(
    items: items ?? this.items,
    totalWeightLabel: totalWeightLabel ?? this.totalWeightLabel,
    photos: photos ?? this.photos,
    uploadingPhotos: uploadingPhotos ?? this.uploadingPhotos,
    pickupAddress: clearPickupAddress ? null : pickupAddress ?? this.pickupAddress,
    profileIncomplete: profileIncomplete ?? this.profileIncomplete,
    itemsError: clearItemsError ? null : itemsError ?? this.itemsError,
    publishError: clearPublishError ? null : publishError ?? this.publishError,
    publishing: publishing ?? this.publishing,
    published: published ?? this.published,
    publishedOrderId: publishedOrderId ?? this.publishedOrderId,
  );
}
