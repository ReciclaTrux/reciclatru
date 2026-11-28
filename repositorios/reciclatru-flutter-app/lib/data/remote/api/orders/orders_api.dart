/// Declara las llamadas HTTP de orders api con los mismos endpoints que consume la app Kotlin.
import 'package:dio/dio.dart';

import '../../api_urls.dart';
import '../../dto/orders/order_dtos.dart';
import '../../../../model/orders/order_acceptance.dart';
import '../../../../model/orders/order_summary.dart';

/// Endpoints Retrofit-equivalentes de pedidos.
class OrdersApi {
  const OrdersApi(this._dio);
  final Dio _dio;

  Future<ServicingOrderOutputDto> createOrder(ServicingOrderInitiateRequestDto body) async =>
      _output(await _dio.post('/servicing-order/initiation', data: body.toJson()));
  Future<ServicingOrderListOutputDto> getMyOrders({required String tab, required int limit, String? cursor}) async {
    final response = await _dio.get('/servicing-order/retrieval', queryParameters: {
      'tab': tab, 'limit': limit, if (cursor != null) 'cursor': cursor,
    });
    final data = Map<String, dynamic>.from(response.data as Map);
    final rows = data['items'];
    return ServicingOrderListOutputDto(
      items: rows is List ? rows.whereType<Map>().map((row) => _summary(Map<String, dynamic>.from(row))).toList() : const [],
      nextCursor: data['nextCursor']?.toString(),
    );
  }
  Future<ServicingOrderOutputDto> getOrder(String id) async => _output(await _dio.get('/servicing-order/$id/retrieval'));
  Future<ServicingOrderOutputDto> cancelOrder(String id) async => _output(await _dio.put('/servicing-order/$id/termination'));
  Future<ServicingOrderOutputDto> updateOrder(String id, ServicingOrderUpdateRequestDto body) async =>
      _output(await _dio.put('/servicing-order/$id/update', data: body.toJson()));
  Future<void> removePhoto(String id, String photoId) async {
    await _dio.put('/servicing-order/$id/photo/$photoId/termination');
  }
  Future<AcceptanceListOutputDto> getAcceptances(String id) async {
    final response = await _dio.get('/servicing-order/$id/acceptance/retrieval');
    final raw = (response.data as Map)['items'];
    final items = raw is! List ? const <OrderAcceptance>[] : raw.whereType<Map>().map((row) {
      final value = Map<String, dynamic>.from(row);
      final status = switch (value['status']?.toString().toUpperCase()) {
        'CHOSEN' => AcceptanceStatus.chosen,
        'DISCARDED' => AcceptanceStatus.discarded,
        _ => AcceptanceStatus.accepted,
      };
      return OrderAcceptance(
        id: value['id']?.toString() ?? '',
        status: status,
        fullName: value['fullName']?.toString() ?? '',
        phone: value['phone']?.toString() ?? '',
        acceptedAt: DateTime.tryParse(value['acceptedAt']?.toString() ?? ''),
      );
    }).toList();
    return AcceptanceListOutputDto(items);
  }
  Future<ServicingOrderOutputDto> chooseRecycler(String id, ChooseRecyclerRequestDto body) async =>
      _output(await _dio.put('/servicing-order/$id/confirmation/execution', data: body.toJson()));
  Future<OrderPhotoDto> uploadPhoto(String id, MultipartFile file) async {
    final response = await _dio.post('/servicing-order/$id/photo/initiation', data: FormData.fromMap({'file': file}));
    return OrderPhotoDto.fromJson(
      withAbsoluteApiImageUrls(Map<String, dynamic>.from(response.data as Map)),
    );
  }

  ServicingOrderOutputDto _output(Response<dynamic> response) =>
      ServicingOrderOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  OrderSummary _summary(Map<String, dynamic> json) =>
      OrderSummary.fromJson(withAbsoluteApiImageUrls(json));
}
