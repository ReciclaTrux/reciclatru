/// Implementa el repositorio explore repository impl conectando los contratos del dominio con sus fuentes de datos.
import 'package:dio/dio.dart';
import '../../../domain/repository/explore/explore_repository.dart';
import '../../../model/explore/available_order.dart';
import '../../../model/explore/available_order_detail.dart';
import '../../remote/network_exception.dart';
import '../../remote/api/explore/explore_api.dart';
import '../../remote/mapper/explore/explore_mapper.dart';
import '../../remote/mapper/explore/available_order_detail_mapper.dart';

/// Implementación REST de la exploración que corresponde al rol reciclador.
class ExploreRepositoryImpl implements ExploreRepository {
  ExploreRepositoryImpl(Dio dio) : _api = ExploreApi(dio);
  final ExploreApi _api;

  @override
  Future<AvailableOrdersPage> getAvailableOrders(GeoPoint? location, {String? cursor}) async {
    final dto = await apiCall(() => _api.getAvailableOrders(
      latitude: location?.latitude,
      longitude: location?.longitude,
      cursor: cursor,
    ));
    return mapAvailableOrders(dto);
  }

  @override
  Future<AvailableOrderDetail> getOrderDetail(String id, GeoPoint? location) async => mapAvailableOrderDetail(
    await apiCall(() => _api.getOrderDetail(
      id,
      latitude: location?.latitude,
      longitude: location?.longitude,
    )),
  );

  @override
  Future<AvailableOrderDetail> acceptOrder(String id) async => mapAvailableOrderDetail(
    await apiCall(() => _api.acceptOrder(id)),
  );
}
