/// Coordina las acciones y el estado de health view model siguiendo el ViewModel equivalente de Kotlin.
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:reciclatru/di/health_module.dart';
import 'package:reciclatru/data/remote/api/health/health_api.dart';

class HealthViewModel extends AsyncNotifier<HealthStatus> {
  @override
  Future<HealthStatus> build() => ref.read(healthApiProvider).check();

  Future<void> retry() async {
    state = const AsyncLoading<HealthStatus>();
    state = await AsyncValue.guard<HealthStatus>(
      () => ref.read(healthApiProvider).check(),
    );
  }
}

final backendStatusProvider =
    AsyncNotifierProvider<HealthViewModel, HealthStatus>(
      HealthViewModel.new,
      retry: (retryCount, error) => null,
    );
