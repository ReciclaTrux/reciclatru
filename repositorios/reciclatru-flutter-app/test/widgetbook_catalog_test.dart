import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

import '../widgetbook/main.dart';

/// Smoke test del catálogo: cada caso debe montarse sin excepciones.
///
/// `RtMapPicker` se excluye porque usa una vista de plataforma de MapLibre que
/// un test de widgets no puede hospedar; se verifica en un dispositivo.
void main() {
  const routes = <String, String>{
    // Átomos
    'botones variantes': '/?path=átomos/rtbutton/variantes',
    'botones estados': '/?path=átomos/rtbutton/estados',
    'botones interactivo': '/?path=átomos/rtbutton/interactivo',
    'texto variantes': '/?path=átomos/rttextfield/variantes',
    'texto interactivo': '/?path=átomos/rttextfield/interactivo',
    'spinner tamaños': '/?path=átomos/rtspinner/tamaños',
    'spinner interactivo': '/?path=átomos/rtspinner/interactivo',
    'iconos variantes': '/?path=átomos/rticonbutton/variantes',
    'iconos interactivo': '/?path=átomos/rticonbutton/interactivo',
    'avatar iniciales': '/?path=átomos/rtavatar/iniciales',
    'avatar interactivo': '/?path=átomos/rtavatar/interactivo',
    'badge variantes': '/?path=átomos/rtbadge/variantes',
    'badge interactivo': '/?path=átomos/rtbadge/interactivo',
    // Moléculas
    'rol estados': '/?path=moléculas/rtroleselector/estados',
    'rol interactivo': '/?path=moléculas/rtroleselector/interactivo',
    'alert tonos': '/?path=moléculas/rtinlinealert/cuatro-tonos',
    'alert interactivo': '/?path=moléculas/rtinlinealert/interactivo',
    'search estados': '/?path=moléculas/rtsearchfield/estados',
    'search sugerencias': '/?path=moléculas/rtsearchfield/sugerencias',
    'search interactivo': '/?path=moléculas/rtsearchfield/interactivo',
    'contact estados': '/?path=moléculas/rtcontactrow/estados',
    'contact interactivo': '/?path=moléculas/rtcontactrow/interactivo',
    'dialog estados': '/?path=moléculas/rtconfirmdialog/estados',
    'dialog interactivo': '/?path=moléculas/rtconfirmdialog/interactivo',
    'snack estados': '/?path=moléculas/rtsnackbar/estados',
    'snack interactivo': '/?path=moléculas/rtsnackbar/interactivo',
    'error estados': '/?path=moléculas/rterrorstate/estados',
    'error interactivo': '/?path=moléculas/rterrorstate/interactivo',
    // Organismos
    'appbar estados': '/?path=organismos/rttopappbar/estados',
    'appbar interactivo': '/?path=organismos/rttopappbar/interactivo',
    'nav roles': '/?path=organismos/rtbottomnav/roles',
    'nav interactivo': '/?path=organismos/rtbottomnav/interactivo',
    'action estados': '/?path=organismos/rtactionbar/estados',
    'action interactivo': '/?path=organismos/rtactionbar/interactivo',
  };

  for (final entry in routes.entries) {
    testWidgets(entry.key, (tester) async {
      await tester.binding.setSurfaceSize(const Size(390, 844));
      addTearDown(() => tester.binding.setSurfaceSize(null));

      await tester.pumpWidget(
        DesignSystemApp(
          key: ValueKey<String>(entry.key),
          initialRoute: entry.value,
        ),
      );
      await tester.pump();
      await tester.pump(const Duration(milliseconds: 200));

      expect(tester.takeException(), isNull, reason: entry.key);
    });
  }
}
