import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:reciclatru/core/design_system/molecules/rt_search_field.dart';
import 'package:reciclatru/core/design_system/theme/rt_theme.dart';

void main() {
  testWidgets('RtSearchField selecciona una sugerencia', (tester) async {
    String? selected;

    Widget build({required String query, required List<String> suggestions}) {
      return MaterialApp(
        theme: rtTheme,
        home: Scaffold(
          body: Padding(
            padding: const EdgeInsets.all(16),
            child: RtSearchField(
              query: query,
              onQueryChange: (_) {},
              onSearch: () {},
              suggestions: suggestions,
              onSuggestionSelected: (suggestion) => selected = suggestion,
            ),
          ),
        ),
      );
    }

    await tester.pumpWidget(build(query: '', suggestions: const <String>[]));
    await tester.tap(find.byType(TextField));
    await tester.pump();
    await tester.enterText(find.byType(TextField), 'Larco');
    await tester.pump();

    // La feature entrega una lista nueva después del debounce.
    await tester.pumpWidget(
      build(
        query: 'Larco',
        suggestions: <String>['Av. Larco 1250', 'Plaza de Armas'],
      ),
    );
    await tester.pump();

    expect(find.text('Av. Larco 1250'), findsOneWidget);
    await tester.tap(find.text('Av. Larco 1250'));
    await tester.pump();

    expect(selected, 'Av. Larco 1250');
  });
}
