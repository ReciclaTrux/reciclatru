import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'health_api.dart';

class BackendStatusNotifier extends AsyncNotifier<HealthStatus> {
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
    AsyncNotifierProvider<BackendStatusNotifier, HealthStatus>(
      BackendStatusNotifier.new,
      retry: (retryCount, error) => null,
    );
