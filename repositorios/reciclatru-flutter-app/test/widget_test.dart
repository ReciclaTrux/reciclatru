import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reciclatru/core/design_system/atoms/rt_avatar.dart';
import 'package:reciclatru/core/design_system/atoms/rt_button.dart';
import 'package:reciclatru/core/design_system/atoms/rt_text_field.dart';
import 'package:reciclatru/core/design_system/theme/rt_colors.dart';
import 'package:reciclatru/core/design_system/theme/rt_theme.dart';
import 'package:reciclatru/main.dart';

void main() {
  testWidgets('la app arranca con el tema del sistema', (tester) async {
    await tester.pumpWidget(const ReciclaTruApp());

    expect(find.text('ReciclaTru'), findsOneWidget);

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

Widget _wrap(Widget child) {
  return MaterialApp(
    theme: rtTheme,
    home: Scaffold(
      body: Padding(padding: const EdgeInsets.all(16), child: child),
    ),
  );
}
