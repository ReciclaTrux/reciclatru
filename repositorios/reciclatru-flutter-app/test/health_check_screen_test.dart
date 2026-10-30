import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reciclatru/core/design_system/molecules/rt_error_state.dart';
import 'package:reciclatru/core/design_system/theme/rt_theme.dart';
import 'package:reciclatru/core/network/api_exception.dart';
import 'package:reciclatru/core/network/health_api.dart';
import 'package:reciclatru/dev/health_check_screen.dart';

class _FakeHealthApi implements HealthApi {
  _FakeHealthApi({this.error});

  final ApiException? error;

  @override
  Future<HealthStatus> check() async {
    if (error != null) throw error!;
    return const HealthStatus(
      statusCode: 200,
      elapsed: Duration(milliseconds: 120),
      status: 'ok',
    );
  }
}

Widget _app(HealthApi api) => ProviderScope(
  overrides: [healthApiProvider.overrideWithValue(api)],
  child: MaterialApp(theme: rtTheme, home: const HealthCheckScreen()),
);

void main() {
  testWidgets('muestra que la API responde cuando el health es correcto', (
    tester,
  ) async {
    await tester.pumpWidget(_app(_FakeHealthApi()));
    await tester.tap(find.byKey(const Key('btn_probar_conexion')));
    await tester.pump();
    await tester.pump();

    expect(find.text('La API responde'), findsOneWidget);
    expect(find.textContaining('HTTP 200'), findsOneWidget);
  });

  testWidgets('muestra el error con Reintentar cuando no hay conexión', (
    tester,
  ) async {
    await tester.pumpWidget(
      _app(
        _FakeHealthApi(
          error: const ApiException(
            kind: ApiFailureKind.noConnection,
            message: 'No pudimos conectar con la API.',
          ),
        ),
      ),
    );
    await tester.tap(find.byKey(const Key('btn_probar_conexion')));
    await tester.pump();
    await tester.pump();

    expect(find.byType(RtErrorState), findsOneWidget);
    expect(find.text('Reintentar'), findsOneWidget);
  });
}
