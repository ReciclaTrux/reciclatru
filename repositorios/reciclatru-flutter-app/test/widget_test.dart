import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_avatar.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_text_field.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_colors.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_theme.dart';
import 'package:reciclatru/data/remote/api/health/health_api.dart';
import 'package:reciclatru/di/health_module.dart';
import 'package:reciclatru/main.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    SharedPreferences.setMockInitialValues({});
  });

  testWidgets('la app arranca con el tema del sistema', (tester) async {
    await tester.pumpWidget(
      ProviderScope(
        overrides: [healthApiProvider.overrideWithValue(_OkHealthApi())],
        child: const ReciclaTruApp(),
      ),
    );

    expect(find.text('Hola de nuevo'), findsOneWidget);
    expect(find.byKey(const Key('input_correo')), findsOneWidget);

    final context = tester.element(find.byType(Scaffold));
    expect(Theme.of(context).extension<RtExtraColors>(), isNotNull);
    expect(Theme.of(context).colorScheme.primary, rtColorScheme.primary);
  });

  testWidgets('RtAvatar usa las iniciales del nombre y apellido', (
      tester,
      ) async {
    await tester.pumpWidget(_wrap(const RtAvatar(name: 'Juan Pérez')));

    expect(find.text('JP'), findsOneWidget);
  });

  testWidgets('RtButton deshabilitado no ejecuta la acción', (tester) async {
    var tapped = false;

    await tester.pumpWidget(
      _wrap(
        RtButton(
          text: 'Publicar pedido',
          enabled: false,
          onClick: () => tapped = true,
        ),
      ),
    );

    await tester.tap(find.text('Publicar pedido'));
    expect(tapped, isFalse);
  });

  testWidgets('RtTextField muestra el contador de caracteres', (tester) async {
    await tester.pumpWidget(
      _wrap(
        RtTextField(
          label: 'Referencia',
          value: 'Dpto. 302',
          onValueChange: (_) {},
          maxLength: 150,
        ),
      ),
    );

    expect(find.text('9/150'), findsOneWidget);
  });
}

class _OkHealthApi implements HealthApi {
  @override
  Future<HealthStatus> check() async => const HealthStatus(
    statusCode: 200,
    elapsed: Duration(milliseconds: 100),
    status: 'UP',
  );
}

Widget _wrap(Widget child) {
  return MaterialApp(
    theme: rtTheme,
    home: Scaffold(
      body: Padding(padding: const EdgeInsets.all(16), child: child),
    ),
  );
}
