import 'package:flutter/material.dart';
import 'package:reciclatru/core/design_system/atoms/rt_acceptance_chip.dart';
import 'package:reciclatru/core/design_system/molecules/rt_address_block.dart';
import 'package:reciclatru/core/design_system/organisms/rt_acceptance_card.dart';
import 'package:reciclatru/core/design_system/organisms/rt_accepted_order_card.dart';
import 'package:reciclatru/core/design_system/organisms/rt_available_order_card.dart';
import 'package:reciclatru/core/design_system/organisms/rt_location_prompt.dart';
import 'package:reciclatru/core/design_system/organisms/rt_photo_gallery.dart';
import 'package:reciclatru/core/design_system/theme/rt_colors.dart';
import 'package:reciclatru/core/design_system/theme/rt_spacing.dart';
import 'package:widgetbook/widgetbook.dart';

/// Casos del lote 4: HU-10 a HU-16.
final List<WidgetbookNode> lote4AtomComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtAcceptanceChip',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Etiquetas',
        builder: (_) => _preview(const _AcceptanceChipStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final label = context.knobs.object.dropdown<RtAcceptanceLabel>(
            label: 'Etiqueta',
            options: RtAcceptanceLabel.values,
            initialOption: RtAcceptanceLabel.chosen,
            labelBuilder: (value) => value.label,
          );
          return _preview(RtAcceptanceChip(label: label));
        },
      ),
    ],
  ),
];

final List<WidgetbookNode> lote4MoleculeComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtAddressBlock',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _AddressBlockStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final address = context.knobs.string(
            label: 'Dirección (vacío = sin dirección)',
            initialValue: 'Av. Larco 1250',
          );
          final district = context.knobs.string(
            label: 'Distrito',
            initialValue: 'Víctor Larco Herrera',
          );
          final reference = context.knobs.string(
            label: 'Referencia (vacío = oculta)',
            initialValue: 'Dpto. 302, puerta negra, frente al parque',
          );
          return _preview(
            _AddressBlockPlayground(
              address: address.trim().isEmpty ? null : address,
              district: district,
              reference: reference.trim().isEmpty ? null : reference,
            ),
          );
        },
      ),
    ],
  ),
];

final List<WidgetbookNode> lote4OrganismComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtAvailableOrderCard',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _AvailableOrderCardStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final district = context.knobs.string(
            label: 'Distrito',
            initialValue: 'Víctor Larco Herrera',
          );
          final withDistance = context.knobs.boolean(
            label: 'Con ubicación (distancia)',
            initialValue: true,
          );
          final distance = context.knobs.double.slider(
            label: 'Distancia (km)',
            initialValue: 1.2,
            min: 0.1,
            max: 20,
          );
          final weight = context.knobs.double.slider(
            label: 'Peso (kg)',
            initialValue: 14.5,
            min: 0.5,
            max: 100,
          );
          final items = context.knobs.int.slider(
            label: 'Ítems',
            initialValue: 3,
            min: 1,
            max: 10,
          );
          final ago = context.knobs.string(
            label: 'Antigüedad',
            initialValue: 'hace 2 h',
          );
          final accepted = context.knobs.boolean(
            label: 'Ya aceptado',
            initialValue: false,
          );
          return _preview(
            _AvailableOrderCardPlayground(
              district: district,
              distanceKm: withDistance ? distance : null,
              weight: weight,
              items: items,
              ago: ago,
              accepted: accepted,
            ),
          );
        },
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtLocationPrompt',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) =>
            _preview(const RtLocationPrompt(onContinue: _noop, onSkip: _noop)),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (_) => _preview(const _LocationPromptPlayground()),
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtPhotoGallery',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Galería y visor',
        builder: (_) => _preview(const _PhotoGalleryStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final count = context.knobs.int.slider(
            label: 'Fotos',
            initialValue: 3,
            min: 0,
            max: 8,
          );
          final broken = context.knobs.boolean(
            label: 'Última foto no carga',
            initialValue: true,
          );
          return _preview(
            _PhotoGalleryPlayground(count: count, lastBroken: broken),
          );
        },
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtAcceptanceCard',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _AcceptanceCardStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final name = context.knobs.string(
            label: 'Reciclador',
            initialValue: 'Juan Pérez',
          );
          final phone = context.knobs.string(
            label: 'Celular',
            initialValue: '987654321',
          );
          final at = context.knobs.string(
            label: 'Aceptó',
            initialValue: 'hoy, 9:40',
          );
          final choosing = context.knobs.boolean(
            label: 'Eligiendo (carga)',
            initialValue: false,
          );
          final enabled = context.knobs.boolean(
            label: 'Elegir habilitado',
            initialValue: true,
          );
          return _preview(
            _AcceptanceCardPlayground(
              name: name,
              phone: phone,
              acceptedAt: at,
              choosing: choosing,
              chooseEnabled: enabled,
            ),
          );
        },
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtAcceptedOrderCard',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _AcceptedOrderCardStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: (context) {
          final label = context.knobs.object.dropdown<RtAcceptanceLabel>(
            label: 'Etiqueta',
            options: RtAcceptanceLabel.values,
            initialOption: RtAcceptanceLabel.chosen,
            labelBuilder: (value) => value.label,
          );
          final district = context.knobs.string(
            label: 'Distrito',
            initialValue: 'Víctor Larco Herrera',
          );
          final weight = context.knobs.double.slider(
            label: 'Peso (kg)',
            initialValue: 14.5,
            min: 0.5,
            max: 100,
          );
          final at = context.knobs.string(
            label: 'Aceptado',
            initialValue: 'Aceptado hoy',
          );
          return _preview(
            _AcceptedOrderCardPlayground(
              label: label,
              district: district,
              weight: weight,
              acceptedAt: at,
            ),
          );
        },
      ),
    ],
  ),
];

Widget _preview(Widget child) {
  return ColoredBox(
    color: rtBackground,
    child: SingleChildScrollView(
      padding: const EdgeInsets.all(RtSpacing.s4),
      child: child,
    ),
  );
}

void _noop() {}

void _noopIndex(int index) {}

const String _photoUrl = 'https://picsum.photos/seed/reciclatru/160';
const String _brokenPhotoUrl = 'https://invalid.invalid/foto.jpg';

Widget _label(BuildContext context, String text) {
  return Padding(
    padding: const EdgeInsets.only(bottom: RtSpacing.s2),
    child: Text(text, style: Theme.of(context).textTheme.labelLarge),
  );
}

Widget _counter(BuildContext context, String text) {
  return Padding(
    padding: const EdgeInsets.only(top: RtSpacing.s3),
    child: Text(text, style: Theme.of(context).textTheme.bodyMedium),
  );
}

// ---------------------------------------------------------------------------
// Estados estáticos
// ---------------------------------------------------------------------------

class _AcceptanceChipStates extends StatelessWidget {
  const _AcceptanceChipStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        _label(context, 'Estado combinado para el reciclador (HU-14)'),
        Wrap(
          spacing: RtSpacing.s2,
          runSpacing: RtSpacing.s2,
          children: <Widget>[
            for (final label in RtAcceptanceLabel.values)
              RtAcceptanceChip(label: label),
          ],
        ),
      ],
    );
  }
}

class _AddressBlockStates extends StatelessWidget {
  const _AddressBlockStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _label(context, 'Con dirección y referencia'),
        const RtAddressBlock(
          address: 'Av. Larco 1250',
          district: 'Víctor Larco Herrera',
          reference: 'Dpto. 302, puerta negra, frente al parque',
          lat: -8.1122,
          lng: -79.0274,
          onDirections: _noop,
          onCopy: _noop,
        ),
        const SizedBox(height: RtSpacing.s5),
        _label(context, 'Sin dirección escrita'),
        const RtAddressBlock(
          address: null,
          district: 'Trujillo',
          reference: 'Frente al parque, casa verde',
          lat: -8.1122,
          lng: -79.0274,
          onDirections: _noop,
          onCopy: _noop,
        ),
        const SizedBox(height: RtSpacing.s5),
        _label(context, 'Sin referencia'),
        const RtAddressBlock(
          address: 'Av. Larco 1250',
          district: 'Víctor Larco Herrera',
          lat: -8.1122,
          lng: -79.0274,
          onDirections: _noop,
          onCopy: _noop,
        ),
      ],
    );
  }
}

class _AvailableOrderCardStates extends StatelessWidget {
  const _AvailableOrderCardStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAvailableOrderCard(
          id: 'a1',
          district: 'Víctor Larco Herrera',
          distanceKm: 1.2,
          totalWeightKg: 14.5,
          itemsCount: 3,
          photoUrl: _photoUrl,
          publishedAgo: 'hace 2 h',
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAvailableOrderCard(
          id: 'a2',
          district: 'Trujillo',
          distanceKm: 3.8,
          totalWeightKg: 4,
          itemsCount: 1,
          publishedAgo: 'hace 20 min',
          alreadyAccepted: true,
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAvailableOrderCard(
          id: 'a3',
          district: 'Sin ubicación del reciclador',
          totalWeightKg: 7.5,
          itemsCount: 2,
          photoUrl: _photoUrl,
          publishedAgo: 'hace 1 día',
          onClick: _noop,
        ),
      ],
    );
  }
}

class _PhotoGalleryStates extends StatelessWidget {
  const _PhotoGalleryStates();

  @override
  Widget build(BuildContext context) {
    const photos = <String>[_photoUrl, _photoUrl, _brokenPhotoUrl];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _label(context, 'Tres fotos, la última no carga'),
        const RtPhotoGallery(photos: photos, onOpen: _noopIndex),
        const SizedBox(height: RtSpacing.s5),
        _label(context, 'Sin fotos (la sección no aparece)'),
        const RtPhotoGallery(photos: <String>[], onOpen: _noopIndex),
        const SizedBox(height: RtSpacing.s5),
        _label(context, 'Visor a pantalla completa'),
        SizedBox(
          height: 320,
          child: ClipRRect(
            borderRadius: BorderRadius.circular(16),
            child: const RtPhotoViewer(photos: photos, initialIndex: 1),
          ),
        ),
      ],
    );
  }
}

class _AcceptanceCardStates extends StatelessWidget {
  const _AcceptanceCardStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        _label(context, 'Recicladores que aceptaron (3)'),
        const RtAcceptanceCard(
          recyclerName: 'Juan Pérez',
          phone: '987654321',
          acceptedAt: 'hoy, 9:40',
          onCall: _noop,
          onChoose: _noop,
        ),
        const SizedBox(height: RtSpacing.s3),
        const RtAcceptanceCard(
          recyclerName: 'María Ríos',
          phone: '956111204',
          acceptedAt: 'hoy, 10:42',
          onCall: _noop,
          onChoose: _noop,
          choosing: true,
        ),
        const SizedBox(height: RtSpacing.s3),
        const RtAcceptanceCard(
          recyclerName: 'Luis Alberto Quispe Mamani',
          phone: '944222333',
          acceptedAt: 'hoy, 11:05',
          onCall: _noop,
          onChoose: _noop,
          chooseEnabled: false,
        ),
      ],
    );
  }
}

class _AcceptedOrderCardStates extends StatelessWidget {
  const _AcceptedOrderCardStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAcceptedOrderCard(
          orderId: 'c1',
          district: 'Víctor Larco Herrera',
          totalWeightKg: 14.5,
          acceptedAt: 'Aceptado hoy',
          label: RtAcceptanceLabel.chosen,
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAcceptedOrderCard(
          orderId: 'c2',
          district: 'Trujillo',
          totalWeightKg: 4,
          acceptedAt: 'Aceptado ayer',
          label: RtAcceptanceLabel.waiting,
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAcceptedOrderCard(
          orderId: 'c3',
          district: 'La Esperanza',
          totalWeightKg: 6,
          acceptedAt: 'Aceptado el 10 nov',
          label: RtAcceptanceLabel.notChosen,
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAcceptedOrderCard(
          orderId: 'c4',
          district: 'El Porvenir',
          totalWeightKg: 9,
          acceptedAt: 'Aceptado el 8 nov',
          label: RtAcceptanceLabel.cancelledByNeighbor,
          onClick: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtAcceptedOrderCard(
          orderId: 'c5',
          district: 'Huanchaco',
          totalWeightKg: 12,
          acceptedAt: 'Aceptado el 5 nov',
          label: RtAcceptanceLabel.collected,
          onClick: _noop,
        ),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Widgets interactivos
// ---------------------------------------------------------------------------

class _AddressBlockPlayground extends StatefulWidget {
  const _AddressBlockPlayground({
    required this.address,
    required this.district,
    required this.reference,
  });

  final String? address;
  final String district;
  final String? reference;

  @override
  State<_AddressBlockPlayground> createState() =>
      _AddressBlockPlaygroundState();
}

class _AddressBlockPlaygroundState extends State<_AddressBlockPlayground> {
  int _directions = 0;
  int _copies = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAddressBlock(
          address: widget.address,
          district: widget.district,
          reference: widget.reference,
          lat: -8.1122,
          lng: -79.0274,
          onDirections: () => setState(() => _directions++),
          onCopy: () => setState(() => _copies++),
        ),
        _counter(context, 'Cómo llegar: $_directions · Copiar: $_copies'),
      ],
    );
  }
}

class _AvailableOrderCardPlayground extends StatefulWidget {
  const _AvailableOrderCardPlayground({
    required this.district,
    required this.distanceKm,
    required this.weight,
    required this.items,
    required this.ago,
    required this.accepted,
  });

  final String district;
  final double? distanceKm;
  final double weight;
  final int items;
  final String ago;
  final bool accepted;

  @override
  State<_AvailableOrderCardPlayground> createState() =>
      _AvailableOrderCardPlaygroundState();
}

class _AvailableOrderCardPlaygroundState
    extends State<_AvailableOrderCardPlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAvailableOrderCard(
          id: 'playground',
          district: widget.district,
          distanceKm: widget.distanceKm,
          totalWeightKg: widget.weight,
          itemsCount: widget.items,
          photoUrl: _photoUrl,
          publishedAgo: widget.ago,
          alreadyAccepted: widget.accepted,
          onClick: () => setState(() => _taps++),
        ),
        _counter(context, 'Toques en la tarjeta: $_taps'),
      ],
    );
  }
}

class _LocationPromptPlayground extends StatefulWidget {
  const _LocationPromptPlayground();

  @override
  State<_LocationPromptPlayground> createState() =>
      _LocationPromptPlaygroundState();
}

class _LocationPromptPlaygroundState extends State<_LocationPromptPlayground> {
  int _continues = 0;
  int _skips = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtLocationPrompt(
          onContinue: () => setState(() => _continues++),
          onSkip: () => setState(() => _skips++),
        ),
        _counter(context, 'Continuar: $_continues · Ahora no: $_skips'),
      ],
    );
  }
}

class _PhotoGalleryPlayground extends StatefulWidget {
  const _PhotoGalleryPlayground({
    required this.count,
    required this.lastBroken,
  });

  final int count;
  final bool lastBroken;

  @override
  State<_PhotoGalleryPlayground> createState() =>
      _PhotoGalleryPlaygroundState();
}

class _PhotoGalleryPlaygroundState extends State<_PhotoGalleryPlayground> {
  int? _opened;

  @override
  Widget build(BuildContext context) {
    final photos = <String>[
      for (var i = 0; i < widget.count; i++)
        (widget.lastBroken && i == widget.count - 1)
            ? _brokenPhotoUrl
            : _photoUrl,
    ];

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtPhotoGallery(
          photos: photos,
          onOpen: (index) {
            setState(() => _opened = index);
            RtPhotoGallery.showViewer(
              context,
              photos: photos,
              initialIndex: index,
            );
          },
        ),
        _counter(
          context,
          _opened == null
              ? 'Toca una foto para abrir el visor'
              : 'Última foto abierta: ${_opened! + 1}',
        ),
      ],
    );
  }
}

class _AcceptanceCardPlayground extends StatefulWidget {
  const _AcceptanceCardPlayground({
    required this.name,
    required this.phone,
    required this.acceptedAt,
    required this.choosing,
    required this.chooseEnabled,
  });

  final String name;
  final String phone;
  final String acceptedAt;
  final bool choosing;
  final bool chooseEnabled;

  @override
  State<_AcceptanceCardPlayground> createState() =>
      _AcceptanceCardPlaygroundState();
}

class _AcceptanceCardPlaygroundState extends State<_AcceptanceCardPlayground> {
  int _calls = 0;
  int _chosen = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAcceptanceCard(
          recyclerName: widget.name,
          phone: widget.phone,
          acceptedAt: widget.acceptedAt,
          choosing: widget.choosing,
          chooseEnabled: widget.chooseEnabled,
          onCall: () => setState(() => _calls++),
          onChoose: () => setState(() => _chosen++),
        ),
        _counter(context, 'Llamar: $_calls · Elegir: $_chosen'),
      ],
    );
  }
}

class _AcceptedOrderCardPlayground extends StatefulWidget {
  const _AcceptedOrderCardPlayground({
    required this.label,
    required this.district,
    required this.weight,
    required this.acceptedAt,
  });

  final RtAcceptanceLabel label;
  final String district;
  final double weight;
  final String acceptedAt;

  @override
  State<_AcceptedOrderCardPlayground> createState() =>
      _AcceptedOrderCardPlaygroundState();
}

class _AcceptedOrderCardPlaygroundState
    extends State<_AcceptedOrderCardPlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtAcceptedOrderCard(
          orderId: 'playground',
          district: widget.district,
          totalWeightKg: widget.weight,
          acceptedAt: widget.acceptedAt,
          label: widget.label,
          onClick: () => setState(() => _taps++),
        ),
        _counter(context, 'Toques en la tarjeta: $_taps'),
      ],
    );
  }
}
