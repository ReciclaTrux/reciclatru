/// Registra las dependencias de explore module para que ViewModels y repositorios compartan sus implementaciones.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../data/local/explore/fused_device_location_provider.dart';
import '../data/local/explore/shared_prefs_location_prompt_store.dart';
import '../data/repository/explore/explore_repository_impl.dart';
import '../domain/repository/explore/device_location_provider.dart';
import '../domain/repository/explore/explore_repository.dart';
import '../domain/repository/explore/location_prompt_store.dart';
import '../domain/usecase/explore/accept_order_use_case.dart';
import '../domain/usecase/explore/get_available_order_detail_use_case.dart';
import '../domain/usecase/explore/get_available_orders_use_case.dart';
import 'network_module.dart';

/// Provee los adaptadores de red, ubicación del dispositivo y aviso de permisos.
final exploreRepositoryProvider = Provider<ExploreRepository>(
  (ref) => ExploreRepositoryImpl(ref.watch(dioProvider)),
);

/// La ubicación se consulta al actualizar la exploración y nunca se persiste.
final deviceLocationProvider = Provider<DeviceLocationProvider>(
  (ref) => FusedDeviceLocationProvider(),
);

/// Persiste solamente si ya se mostró el aviso de ubicación.
final locationPromptStoreProvider = Provider<LocationPromptStore>(
  (ref) => SharedPrefsLocationPromptStore(),
);

/// Casos de uso consumidos por los ViewModels de exploración.
final getAvailableOrdersUseCaseProvider = Provider<GetAvailableOrdersUseCase>(
  (ref) => GetAvailableOrdersUseCase(ref.watch(exploreRepositoryProvider)),
);
final getAvailableOrderDetailUseCaseProvider = Provider<GetAvailableOrderDetailUseCase>(
  (ref) => GetAvailableOrderDetailUseCase(ref.watch(exploreRepositoryProvider)),
);
final acceptOrderUseCaseProvider = Provider<AcceptOrderUseCase>(
  (ref) => AcceptOrderUseCase(ref.watch(exploreRepositoryProvider)),
);
