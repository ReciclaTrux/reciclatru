/// Registra las dependencias de orders module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/repository/orders/orders_repository_impl.dart';
import '../data/local/orders/in_memory_pending_photos_store.dart';
import '../domain/repository/orders/pending_photos_store.dart';
import '../domain/repository/orders/orders_repository.dart';
import '../domain/usecase/orders/cancel_order_use_case.dart';
import '../domain/usecase/orders/choose_recycler_use_case.dart';
import '../domain/usecase/orders/create_order_use_case.dart';
import '../domain/usecase/orders/get_my_orders_use_case.dart';
import '../domain/usecase/orders/get_order_acceptances_use_case.dart';
import '../domain/usecase/orders/get_order_use_case.dart';
import '../domain/usecase/orders/remove_order_photo_use_case.dart';
import '../domain/usecase/orders/update_order_use_case.dart';
import '../domain/usecase/orders/upload_order_photos_use_case.dart';
import 'package:reciclatru/di/network_module.dart';

/// Inyección de las implementaciones de repositorio para los flujos de pedidos.
final ordersRepositoryProvider = Provider<OrdersRepository>((ref) =>
    OrdersRepositoryImpl(ref.watch(dioProvider)));

/// Mantiene fotos pendientes solo durante la vida del proceso, como Kotlin.
final pendingPhotosStoreProvider = Provider<PendingPhotosStore>((ref) => InMemoryPendingPhotosStore());

/// Casos de uso de pedidos compartidos por sus ViewModels.
final getMyOrdersUseCaseProvider = Provider<GetMyOrdersUseCase>((ref) => GetMyOrdersUseCase(ref.watch(ordersRepositoryProvider)));
final getOrderUseCaseProvider = Provider<GetOrderUseCase>((ref) => GetOrderUseCase(ref.watch(ordersRepositoryProvider)));
final createOrderUseCaseProvider = Provider<CreateOrderUseCase>((ref) => CreateOrderUseCase(ref.watch(ordersRepositoryProvider)));
final updateOrderUseCaseProvider = Provider<UpdateOrderUseCase>((ref) => UpdateOrderUseCase(ref.watch(ordersRepositoryProvider)));
final cancelOrderUseCaseProvider = Provider<CancelOrderUseCase>((ref) => CancelOrderUseCase(ref.watch(ordersRepositoryProvider)));
final getOrderAcceptancesUseCaseProvider = Provider<GetOrderAcceptancesUseCase>((ref) => GetOrderAcceptancesUseCase(ref.watch(ordersRepositoryProvider)));
final chooseRecyclerUseCaseProvider = Provider<ChooseRecyclerUseCase>((ref) => ChooseRecyclerUseCase(ref.watch(ordersRepositoryProvider)));
final uploadOrderPhotosUseCaseProvider = Provider<UploadOrderPhotosUseCase>((ref) => UploadOrderPhotosUseCase(ref.watch(ordersRepositoryProvider), ref.watch(pendingPhotosStoreProvider)));
final removeOrderPhotoUseCaseProvider = Provider<RemoveOrderPhotoUseCase>((ref) => RemoveOrderPhotoUseCase(ref.watch(ordersRepositoryProvider)));
