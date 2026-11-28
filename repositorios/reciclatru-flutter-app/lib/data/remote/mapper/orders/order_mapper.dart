/// Transforma la respuesta de order mapper entre DTOs remotos y modelos de dominio.
import '../../../../model/orders/order.dart';
import '../../../../model/orders/order_summary.dart';
import '../../api_urls.dart';
import '../../dto/orders/order_dtos.dart';

/// Convierte DTOs de pedidos a modelos usados por la capa de presentación.
Order mapOrder(ServicingOrderOutputDto dto) =>
    Order.fromRecord(withAbsoluteApiImageUrls(dto.record));
OrdersPage mapOrderPage(ServicingOrderListOutputDto dto) =>
    OrdersPage(items: dto.items, nextCursor: dto.nextCursor);
