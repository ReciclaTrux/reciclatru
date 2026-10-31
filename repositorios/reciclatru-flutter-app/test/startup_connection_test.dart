import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reciclatru/core/network/api_exception.dart';
import 'package:reciclatru/core/network/health_api.dart';
import 'package:reciclatru/main.dart';

class _FakeHealthApi implements HealthApi {
  _FakeHealthApi({this.error});

  ApiException? error;
  int calls = 0;

  @override
  Future<HealthStatus> check() async {
    calls++;
    if (error != null) throw error!;
    return const HealthStatus(
      statusCode: 200,
      elapsed: Duration(milliseconds: 120),
      status: 'UP',
    );
  }
}

Future<void> _pumpApp(WidgetTester tester, HealthApi api) async {
  await tester.pumpWidget(
    ProviderScope(
      overrides: [healthApiProvider.overrideWithValue(api)],
      child: const ReciclaTruApp(),
    ),
  );
}

void main() {
  testWidgets('muestra Conectado cuando la API responde', (tester) async {
    await _pumpApp(tester, _FakeHealthApi());
    await tester.pump();
    await tester.pump();

    expect(find.text('Conectado'), findsOneWidget);
  });

  testWidgets('muestra el error con Reintentar cuando no hay conexión', (
    tester,
  ) async {
    await _pumpApp(
      tester,
      _FakeHealthApi(
        error: const ApiException(
          kind: ApiFailureKind.noConnection,
          message: 'No pudimos conectar con la API.',
        ),
      ),
    );
    await tester.pump();
    await tester.pump();

    expect(find.text('No pudimos conectar con la API.'), findsOneWidget);
    expect(find.text('Reintentar'), findsOneWidget);
    expect(find.text('Conectado'), findsNothing);
  });

  testWidgets('Reintentar vuelve a consultar y muestra Conectado', (
    tester,
  ) async {
    final api = _FakeHealthApi(
      error: const ApiException(
        kind: ApiFailureKind.noConnection,
        message: 'No pudimos conectar con la API.',
      ),
    );
    await _pumpApp(tester, api);
    await tester.pump();
    await tester.pump();

    api.error = null;
    await tester.tap(find.text('Reintentar'));
    await tester.pump();
    await tester.pump();

    expect(api.calls, 2);
    expect(find.text('Conectado'), findsOneWidget);
  });
}
