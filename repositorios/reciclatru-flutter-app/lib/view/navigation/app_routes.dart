/// Nombres de destino y argumentos que conserva `AppRoutes.kt`.
abstract final class AppRoutes {
  static const start = 'start';
  static const home = 'home';
  static const pickupLocation = 'profile/pickup-location';
  static const register = 'auth/register';
  static const completeProfile = 'profile/complete';
  static const profile = 'profile';
  static const createOrder = 'orders/create';
  static const orderIdArg = 'orderId';
  static const orderDetail = 'orders/detail/{orderId}';
  static const orderPublishedResult = 'order_published';
  static const availableOrderDetail = 'explore/detail/{orderId}';
  static const orderUnavailableResult = 'order_unavailable';
  static const editOrder = 'orders/edit/{orderId}';
  static const orderEditResult = 'order_edit_result';
  static const expiredArg = 'expired';
  static const login = 'auth/login?expired={expired}';

  static String loginFor({bool expired = false}) => 'auth/login?expired=$expired';
  static String orderDetailFor(String orderId) => 'orders/detail/$orderId';
  static String availableOrderDetailFor(String orderId) => 'explore/detail/$orderId';
  static String editOrderFor(String orderId) => 'orders/edit/$orderId';
}
