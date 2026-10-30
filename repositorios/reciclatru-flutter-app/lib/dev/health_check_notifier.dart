import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../core/network/health_api.dart';

/// Estado de la prueba: `null` = aún no se probó.
class HealthCheckNotifier extends AsyncNotifier<HealthStatus?> {
  @override
  FutureOr<HealthStatus?> build() => null;

  Future<void> check() async {
    state = const AsyncLoading<HealthStatus?>();
    state = await AsyncValue.guard<HealthStatus?>(
      () => ref.read(healthApiProvider).check(),
    );
  }
}

final healthCheckProvider =
    AsyncNotifierProvider<HealthCheckNotifier, HealthStatus?>(
      HealthCheckNotifier.new,
    );
