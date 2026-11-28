/// Declara las llamadas HTTP de explore api con los mismos endpoints que consume la app Kotlin.
import 'package:dio/dio.dart';

import '../../dto/explore/explore_dtos.dart';

/// Endpoints de exploración disponibles para el rol reciclador.
class ExploreApi {
  const ExploreApi(this._dio);
  final Dio _dio;

  Future<AvailableOrdersOutputDto> getAvailableOrders({double? latitude, double? longitude, String? cursor}) async {
    final response = await _dio.get('/servicing-order/availability/retrieval', queryParameters: {
      'lat': latitude,
      'lng': longitude,
      'limit': 20,
      if (cursor != null) 'cursor': cursor,
    });
    return AvailableOrdersOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }

  Future<AvailableOrderDetailOutputDto> getOrderDetail(String id, {double? latitude, double? longitude}) async {
    final response = await _dio.get('/servicing-order/$id/retrieval', queryParameters: {
      'lat': latitude,
      'lng': longitude,
    });
    return AvailableOrderDetailOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }

  Future<AvailableOrderDetailOutputDto> acceptOrder(String id) async {
    final response = await _dio.post('/servicing-order/$id/acceptance/initiation');
    return AvailableOrderDetailOutputDto.fromJson(Map<String, dynamic>.from(response.data as Map));
  }
}
