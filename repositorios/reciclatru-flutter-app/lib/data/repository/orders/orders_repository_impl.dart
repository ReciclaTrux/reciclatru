/// Implementa el repositorio orders repository impl conectando los contratos del dominio con sus fuentes de datos.
import 'package:dio/dio.dart';
import 'package:http_parser/http_parser.dart';

import '../../local/orders/photo_compressor.dart';
import '../../../model/orders/order.dart';
import '../../../domain/repository/orders/orders_repository.dart';
import '../../remote/api/orders/orders_api.dart';
import '../../remote/dto/orders/order_dtos.dart';
import '../../remote/mapper/orders/order_mapper.dart';
import '../../remote/network_exception.dart';

/// Traduce las respuestas REST de pedidos a los modelos usados por la app.
class OrdersRepositoryImpl implements OrdersRepository {
  OrdersRepositoryImpl(Dio dio, [PhotoCompressor? photoCompressor])
      : _api = OrdersApi(dio),
        _photoCompressor = photoCompressor ?? PhotoCompressor();
  final OrdersApi _api;
  final PhotoCompressor _photoCompressor;

  @override
  Future<OrdersPage> getMyOrders({required String tab, String? cursor}) async => mapOrderPage(
    await apiCall(() => _api.getMyOrders(tab: tab.toUpperCase(), limit: 20, cursor: cursor)),
  );

  @override
  Future<Order?> getOrder(String id) async => mapOrder(await apiCall(() => _api.getOrder(id)));

  @override
  Future<Order> createOrder(List<OrderItem> items) async => mapOrder(await apiCall(() => _api.createOrder(
    ServicingOrderInitiateRequestDto(items.map((item) => OrderItemDto(
      description: item.description, weightKg: item.weightKg,
    )).toList()),
  )));

  @override
  Future<Order> updateOrder(String id, List<OrderItem> items) async => mapOrder(await apiCall(() => _api.updateOrder(
    id,
    ServicingOrderUpdateRequestDto(items.map((item) => OrderItemDto(
      description: item.description, weightKg: item.weightKg,
    )).toList()),
  )));

  @override
  Future<Order> cancelOrder(String id) async => mapOrder(await apiCall(() => _api.cancelOrder(id)));

  @override
  Future<List<OrderAcceptance>> getAcceptances(String id) async {
    return (await apiCall(() => _api.getAcceptances(id))).items;
  }

  @override
  Future<Order> chooseRecycler(String id, String acceptanceId) async => mapOrder(await apiCall(() => _api.chooseRecycler(
    id,
    ChooseRecyclerRequestDto(acceptanceId),
  )));

  /// Elimina una foto existente mediante la operación de terminación de la API.
  @override
  Future<void> removePhoto(String orderId, String photoId) async {
    await apiCall(() => _api.removePhoto(orderId, photoId));
  }

  /// Sube una imagen elegida en cámara o galería al endpoint del pedido.
  @override
  Future<OrderPhoto> uploadPhoto(String orderId, String filePath) async {
    // Reproduce el límite, la calidad JPEG y la rotación de PhotoCompressor.kt.
    final bytes = await _photoCompressor.compress(filePath);
    final file = MultipartFile.fromBytes(
      bytes,
      filename: 'foto.jpg',
      contentType: MediaType('image', 'jpeg'),
    );
    return (await apiCall(() => _api.uploadPhoto(orderId, file))).photo;
  }

}
