import 'package:flutter/material.dart';
import 'package:reciclatru/core/design_system/atoms/rt_photo_thumb.dart';
import 'package:reciclatru/core/design_system/atoms/rt_status_chip.dart';
import 'package:reciclatru/core/design_system/molecules/rt_empty_state.dart';
import 'package:reciclatru/core/design_system/molecules/rt_item_row.dart';
import 'package:reciclatru/core/design_system/molecules/rt_meta_row.dart';
import 'package:reciclatru/core/design_system/molecules/rt_photo_picker.dart';
import 'package:reciclatru/core/design_system/molecules/rt_tabs.dart';
import 'package:reciclatru/core/design_system/organisms/rt_item_list_editor.dart';
import 'package:reciclatru/core/design_system/organisms/rt_order_card.dart';
import 'package:reciclatru/core/design_system/theme/rt_colors.dart';
import 'package:reciclatru/core/design_system/theme/rt_spacing.dart';
import 'package:widgetbook/widgetbook.dart';

/// Casos del lote 3: HU-05 a HU-09.
final List<WidgetbookNode> lote3AtomComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtPhotoThumb',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Tamaños y estados',
        builder: (_) => _preview(const _PhotoThumbStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildPhotoThumbPlayground,
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtStatusChip',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _StatusChipStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildStatusChipPlayground,
      ),
    ],
  ),
];

final List<WidgetbookNode> lote3MoleculeComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtItemRow',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _ItemRowStates()),
      ),
      WidgetbookUseCase(name: 'Interactivo', builder: _buildItemRowPlayground),
    ],
  ),
  WidgetbookComponent(
    name: 'RtPhotoPicker',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _PhotoPickerStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildPhotoPickerPlayground,
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtTabs',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _TabsStates()),
      ),
      WidgetbookUseCase(name: 'Interactivo', builder: _buildTabsPlayground),
    ],
  ),
  WidgetbookComponent(
    name: 'RtEmptyState',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _EmptyStateStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildEmptyStatePlayground,
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtMetaRow',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Datos',
        builder: (_) => _preview(const _MetaRowStates()),
      ),
      WidgetbookUseCase(name: 'Interactivo', builder: _buildMetaRowPlayground),
    ],
  ),
];

final List<WidgetbookNode> lote3OrganismComponents = <WidgetbookNode>[
  WidgetbookComponent(
    name: 'RtItemListEditor',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _ItemListEditorStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildItemListEditorPlayground,
      ),
    ],
  ),
  WidgetbookComponent(
    name: 'RtOrderCard',
    useCases: <WidgetbookUseCase>[
      WidgetbookUseCase(
        name: 'Estados',
        builder: (_) => _preview(const _OrderCardStates()),
      ),
      WidgetbookUseCase(
        name: 'Interactivo',
        builder: _buildOrderCardPlayground,
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

/// Fotos de muestra del catálogo; no requieren API key.
const String _photoUrl = 'https://picsum.photos/seed/reciclatru/160';
const String _brokenPhotoUrl = 'https://invalid.invalid/foto.jpg';

// ---------------------------------------------------------------------------
// Estados estáticos
// ---------------------------------------------------------------------------

class _PhotoThumbStates extends StatelessWidget {
  const _PhotoThumbStates();

  @override
  Widget build(BuildContext context) {
    return Wrap(
      spacing: RtSpacing.s5,
      runSpacing: RtSpacing.s4,
      crossAxisAlignment: WrapCrossAlignment.end,
      children: <Widget>[
        _labeled(context, 'Con foto (56)', const RtPhotoThumb(url: _photoUrl)),
        _labeled(context, 'Sin foto', const RtPhotoThumb()),
        _labeled(context, 'No cargó', const RtPhotoThumb(url: _brokenPhotoUrl)),
        _labeled(
          context,
          'Quitar (80)',
          RtPhotoThumb(
            url: _photoUrl,
            size: RtPhotoThumbSize.medium,
            onRemove: _noop,
          ),
        ),
        _labeled(
          context,
          'Subiendo (80)',
          const RtPhotoThumb(
            url: _photoUrl,
            size: RtPhotoThumbSize.medium,
            uploading: true,
          ),
        ),
        _labeled(
          context,
          'Galería (112)',
          const RtPhotoThumb(url: _photoUrl, size: RtPhotoThumbSize.large),
        ),
      ],
    );
  }

  Widget _labeled(BuildContext context, String label, Widget child) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        child,
        const SizedBox(height: RtSpacing.s2),
        Text(label, style: Theme.of(context).textTheme.bodyMedium),
      ],
    );
  }
}

class _StatusChipStates extends StatelessWidget {
  const _StatusChipStates();

  @override
  Widget build(BuildContext context) {
    return const Wrap(
      spacing: RtSpacing.s2,
      runSpacing: RtSpacing.s2,
      children: <Widget>[
        RtStatusChip(status: RtOrderStatus.pending),
        RtStatusChip(status: RtOrderStatus.confirmed),
        RtStatusChip(status: RtOrderStatus.collected),
        RtStatusChip(status: RtOrderStatus.cancelled),
      ],
    );
  }
}

class _ItemRowStates extends StatelessWidget {
  const _ItemRowStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtItemRow(
          description: 'Botellas de plástico',
          weightKg: '2.5',
          onChange: _noopItemChange,
          onRemove: _noop,
        ),
        SizedBox(height: RtSpacing.s5),
        RtItemRow(
          description: 'Cartón',
          weightKg: '0',
          onChange: _noopItemChange,
          onRemove: _noop,
          weightError: 'El peso debe ser mayor a 0 y hasta 500 kg',
        ),
        SizedBox(height: RtSpacing.s5),
        RtItemRow(
          description: '',
          weightKg: '',
          onChange: _noopItemChange,
          canRemove: false,
        ),
      ],
    );
  }
}

void _noopItemChange(String description, String weightKg) {}

class _PhotoPickerStates extends StatelessWidget {
  const _PhotoPickerStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtPhotoPicker(
          photos: <RtPhoto>[
            RtPhoto(url: _photoUrl),
            RtPhoto(url: _photoUrl),
          ],
          onAdd: _noop,
          onRemove: _noopIndex,
        ),
        SizedBox(height: RtSpacing.s5),
        RtPhotoPicker(
          photos: <RtPhoto>[
            RtPhoto(url: _photoUrl),
            RtPhoto(url: _photoUrl),
            RtPhoto(url: _photoUrl),
          ],
          onAdd: _noop,
          onRemove: _noopIndex,
        ),
      ],
    );
  }
}

class _TabsStates extends StatelessWidget {
  const _TabsStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtTabs(
          tabs: <RtTabItem>[
            RtTabItem(text: 'Activos', count: 3),
            RtTabItem(text: 'Historial'),
          ],
          selectedIndex: 0,
          onSelect: _noopIndex,
        ),
        SizedBox(height: RtSpacing.s4),
        RtTabs(
          tabs: <RtTabItem>[
            RtTabItem(text: 'Activos', count: 3),
            RtTabItem(text: 'Historial'),
          ],
          selectedIndex: 1,
          onSelect: _noopIndex,
        ),
        SizedBox(height: RtSpacing.s4),
        RtTabs(
          tabs: <RtTabItem>[
            RtTabItem(text: 'Activos'),
            RtTabItem(text: 'Historial'),
          ],
          selectedIndex: 0,
          onSelect: _noopIndex,
        ),
      ],
    );
  }
}

class _EmptyStateStates extends StatelessWidget {
  const _EmptyStateStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      children: <Widget>[
        RtEmptyState(
          icon: Icons.recycling_rounded,
          title: 'Aún no publicaste pedidos',
          message:
              'Publica el material que tienes y un reciclador de tu zona lo '
              'recogerá.',
          actionLabel: 'Publicar pedido',
          onAction: _noop,
        ),
        SizedBox(height: RtSpacing.s5),
        RtEmptyState(
          icon: Icons.recycling_rounded,
          title: 'No hay pedidos disponibles por ahora',
          message: 'Desliza hacia abajo para actualizar la lista.',
        ),
      ],
    );
  }
}

class _MetaRowStates extends StatelessWidget {
  const _MetaRowStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtMetaRow(
          items: <RtMetaItem>[
            RtMetaItem(icon: Icons.near_me_rounded, text: 'a 2.4 km'),
            RtMetaItem(icon: Icons.scale_rounded, text: '14.5 kg'),
            RtMetaItem(icon: Icons.inventory_2_rounded, text: '3 ítems'),
            RtMetaItem(icon: Icons.schedule_rounded, text: 'hace 2 h'),
          ],
        ),
        SizedBox(height: RtSpacing.s4),
        RtMetaRow(
          items: <RtMetaItem>[
            RtMetaItem(icon: Icons.inventory_2_rounded, text: '1 ítem'),
            RtMetaItem(icon: Icons.calendar_today_rounded, text: '12 nov'),
          ],
        ),
      ],
    );
  }
}

class _ItemListEditorStates extends StatelessWidget {
  const _ItemListEditorStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtItemListEditor(
          items: <RtOrderItem>[
            RtOrderItem(description: 'Botellas de plástico', weightKg: '2.5'),
            RtOrderItem(description: 'Cartón', weightKg: '12'),
          ],
          onAdd: _noop,
          onChange: _noopListChange,
          onRemove: _noopIndex,
        ),
        SizedBox(height: RtSpacing.s5),
        RtItemListEditor(
          items: <RtOrderItem>[RtOrderItem(weightKg: '600')],
          onAdd: _noop,
          onChange: _noopListChange,
          onRemove: _noopIndex,
          listError: 'Agrega al menos un ítem',
        ),
      ],
    );
  }
}

void _noopListChange(int index, String description, String weightKg) {}

class _OrderCardStates extends StatelessWidget {
  const _OrderCardStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtOrderCard(
          id: 'p1',
          date: 'Hoy, 9:15',
          status: RtOrderStatus.pending,
          itemsCount: 3,
          totalWeightKg: 14.5,
          photoUrl: _photoUrl,
          acceptances: 3,
          onClick: _noop,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtOrderCard(
          id: 'p2',
          date: 'Ayer, 18:40',
          status: RtOrderStatus.pending,
          itemsCount: 1,
          totalWeightKg: 5,
          onClick: _noop,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtOrderCard(
          id: 'p3',
          date: '10 nov',
          status: RtOrderStatus.confirmed,
          itemsCount: 2,
          totalWeightKg: 9,
          photoUrl: _photoUrl,
          recyclerName: 'Juan Pérez',
          onClick: _noop,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtOrderCard(
          id: 'p4',
          date: '8 nov',
          status: RtOrderStatus.collected,
          itemsCount: 1,
          totalWeightKg: 6,
          onClick: _noop,
        ),
        const SizedBox(height: RtSpacing.s3),
        RtOrderCard(
          id: 'p5',
          date: '5 nov',
          status: RtOrderStatus.cancelled,
          itemsCount: 2,
          totalWeightKg: 7.5,
          onClick: _noop,
        ),
      ],
    );
  }
}

// ---------------------------------------------------------------------------
// Casos interactivos (knobs)
// ---------------------------------------------------------------------------

Widget _buildPhotoThumbPlayground(BuildContext context) {
  final url = context.knobs.string(
    label: 'URL de la foto',
    initialValue: _photoUrl,
  );
  final size = context.knobs.object.dropdown<RtPhotoThumbSize>(
    label: 'Tamaño',
    options: RtPhotoThumbSize.values,
    initialOption: RtPhotoThumbSize.medium,
    labelBuilder: _photoSizeLabel,
  );
  final uploading = context.knobs.boolean(
    label: 'Subiendo',
    initialValue: false,
  );
  final showRemove = context.knobs.boolean(
    label: 'Botón de quitar',
    initialValue: true,
  );

  return _preview(
    _PhotoThumbPlayground(
      url: url.trim().isEmpty ? null : url,
      size: size,
      uploading: uploading,
      showRemove: showRemove,
    ),
  );
}

Widget _buildStatusChipPlayground(BuildContext context) {
  final status = context.knobs.object.dropdown<RtOrderStatus>(
    label: 'Estado',
    options: RtOrderStatus.values,
    initialOption: RtOrderStatus.pending,
    labelBuilder: (value) => value.label,
  );

  return _preview(RtStatusChip(status: status));
}

Widget _buildItemRowPlayground(BuildContext context) {
  final description = context.knobs.string(
    label: 'Material',
    initialValue: 'Botellas de plástico',
  );
  final weight = context.knobs.string(label: 'Peso', initialValue: '2.5');
  final descriptionError = context.knobs.string(
    label: 'Error del material',
    initialValue: '',
  );
  final weightError = context.knobs.string(
    label: 'Error del peso',
    initialValue: '',
  );
  final canRemove = context.knobs.boolean(
    label: 'Se puede quitar',
    initialValue: true,
  );

  return _preview(
    _ItemRowPlayground(
      description: description,
      weightKg: weight,
      descriptionError: descriptionError.trim().isEmpty
          ? null
          : descriptionError,
      weightError: weightError.trim().isEmpty ? null : weightError,
      canRemove: canRemove,
    ),
  );
}

Widget _buildPhotoPickerPlayground(BuildContext context) {
  final initialCount = context.knobs.int.slider(
    label: 'Fotos iniciales',
    initialValue: 2,
    min: 0,
    max: 3,
  );
  final uploading = context.knobs.boolean(
    label: 'Subiendo',
    initialValue: false,
  );

  return _preview(
    _PhotoPickerPlayground(initialCount: initialCount, uploading: uploading),
  );
}

Widget _buildTabsPlayground(BuildContext context) {
  final count = context.knobs.int.slider(
    label: 'Contador de Activos',
    initialValue: 3,
    min: 0,
    max: 9,
  );
  final initialIndex = context.knobs.object.dropdown<int>(
    label: 'Pestaña activa',
    options: const <int>[0, 1],
    initialOption: 0,
    labelBuilder: (index) => index == 0 ? 'Activos' : 'Historial',
  );

  return _preview(_TabsPlayground(initialIndex: initialIndex, count: count));
}

Widget _buildEmptyStatePlayground(BuildContext context) {
  final title = context.knobs.string(
    label: 'Título',
    initialValue: 'Aún no publicaste pedidos',
  );
  final message = context.knobs.string(
    label: 'Mensaje',
    initialValue:
        'Publica el material que tienes y un reciclador de tu zona lo '
        'recogerá.',
  );
  final actionLabel = context.knobs.string(
    label: 'Texto del botón',
    initialValue: 'Publicar pedido',
  );
  final iconLabel = context.knobs.object.dropdown<String>(
    label: 'Ícono',
    options: const <String>['Reciclar', 'Inventario', 'Buscar'],
    initialOption: 'Reciclar',
  );

  return _preview(
    _EmptyStatePlayground(
      title: title,
      message: message,
      actionLabel: actionLabel,
      icon: _emptyIconByLabel(iconLabel),
    ),
  );
}

Widget _buildMetaRowPlayground(BuildContext context) {
  final showDistance = context.knobs.boolean(
    label: 'Distancia',
    initialValue: true,
  );
  final weight = context.knobs.string(label: 'Peso', initialValue: '14.5 kg');
  final items = context.knobs.string(label: 'Ítems', initialValue: '3 ítems');
  final showTime = context.knobs.boolean(label: 'Tiempo', initialValue: true);

  return _preview(
    RtMetaRow(
      items: <RtMetaItem>[
        if (showDistance)
          const RtMetaItem(icon: Icons.near_me_rounded, text: 'a 2.4 km'),
        RtMetaItem(icon: Icons.scale_rounded, text: weight),
        RtMetaItem(icon: Icons.inventory_2_rounded, text: items),
        if (showTime)
          const RtMetaItem(icon: Icons.schedule_rounded, text: 'hace 2 h'),
      ],
    ),
  );
}

Widget _buildItemListEditorPlayground(BuildContext context) {
  final initialCount = context.knobs.int.slider(
    label: 'Ítems iniciales',
    initialValue: 2,
    min: 1,
    max: RtItemListEditor.maxItems,
  );
  final showListError = context.knobs.boolean(
    label: 'Error de lista',
    initialValue: false,
  );

  return _preview(
    _ItemListEditorPlayground(
      initialCount: initialCount,
      listError: showListError ? 'Agrega al menos un ítem' : null,
    ),
  );
}

Widget _buildOrderCardPlayground(BuildContext context) {
  final status = context.knobs.object.dropdown<RtOrderStatus>(
    label: 'Estado',
    options: RtOrderStatus.values,
    initialOption: RtOrderStatus.pending,
    labelBuilder: (value) => value.label,
  );
  final acceptances = context.knobs.int.slider(
    label: 'Aceptaciones',
    initialValue: 3,
    min: 0,
    max: 9,
  );
  final recycler = context.knobs.string(
    label: 'Reciclador elegido',
    initialValue: 'Juan Pérez',
  );
  final showPhoto = context.knobs.boolean(
    label: 'Con foto',
    initialValue: true,
  );

  return _preview(
    _OrderCardPlayground(
      status: status,
      acceptances: acceptances,
      recyclerName: recycler.trim().isEmpty ? null : recycler,
      showPhoto: showPhoto,
    ),
  );
}

// ---------------------------------------------------------------------------
// Widgets interactivos
// ---------------------------------------------------------------------------

class _PhotoThumbPlayground extends StatefulWidget {
  const _PhotoThumbPlayground({
    required this.url,
    required this.size,
    required this.uploading,
    required this.showRemove,
  });

  final String? url;
  final RtPhotoThumbSize size;
  final bool uploading;
  final bool showRemove;

  @override
  State<_PhotoThumbPlayground> createState() => _PhotoThumbPlaygroundState();
}

class _PhotoThumbPlaygroundState extends State<_PhotoThumbPlayground> {
  int _removals = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        RtPhotoThumb(
          url: widget.url,
          size: widget.size,
          uploading: widget.uploading,
          onRemove: widget.showRemove
              ? () => setState(() => _removals++)
              : null,
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Veces quitada: $_removals',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _ItemRowPlayground extends StatefulWidget {
  const _ItemRowPlayground({
    required this.description,
    required this.weightKg,
    required this.descriptionError,
    required this.weightError,
    required this.canRemove,
  });

  final String description;
  final String weightKg;
  final String? descriptionError;
  final String? weightError;
  final bool canRemove;

  @override
  State<_ItemRowPlayground> createState() => _ItemRowPlaygroundState();
}

class _ItemRowPlaygroundState extends State<_ItemRowPlayground> {
  late String _description;
  late String _weight;
  int _removals = 0;

  @override
  void initState() {
    super.initState();
    _description = widget.description;
    _weight = widget.weightKg;
  }

  @override
  void didUpdateWidget(covariant _ItemRowPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.description != oldWidget.description) {
      _description = widget.description;
    }
    if (widget.weightKg != oldWidget.weightKg) {
      _weight = widget.weightKg;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtItemRow(
          description: _description,
          weightKg: _weight,
          descriptionError: widget.descriptionError,
          weightError: widget.weightError,
          canRemove: widget.canRemove,
          onChange: (description, weight) => setState(() {
            _description = description;
            _weight = weight;
          }),
          onRemove: () => setState(() => _removals++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Valores: "$_description" · $_weight kg · quitado $_removals veces',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _PhotoPickerPlayground extends StatefulWidget {
  const _PhotoPickerPlayground({
    required this.initialCount,
    required this.uploading,
  });

  final int initialCount;
  final bool uploading;

  @override
  State<_PhotoPickerPlayground> createState() => _PhotoPickerPlaygroundState();
}

class _PhotoPickerPlaygroundState extends State<_PhotoPickerPlayground> {
  late List<RtPhoto> _photos;
  int _added = 0;

  @override
  void initState() {
    super.initState();
    _photos = _buildPhotos(widget.initialCount);
  }

  @override
  void didUpdateWidget(covariant _PhotoPickerPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialCount != oldWidget.initialCount ||
        widget.uploading != oldWidget.uploading) {
      _photos = _buildPhotos(widget.initialCount);
    }
  }

  List<RtPhoto> _buildPhotos(int count) {
    return List<RtPhoto>.generate(
      count,
      (index) => RtPhoto(url: _photoUrl, uploading: widget.uploading),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtPhotoPicker(
          photos: _photos,
          onAdd: () => setState(() {
            if (_photos.length < 3) {
              _photos = <RtPhoto>[..._photos, const RtPhoto(url: _photoUrl)];
              _added++;
            }
          }),
          onRemove: (index) => setState(() {
            _photos = <RtPhoto>[..._photos]..removeAt(index);
          }),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Fotos: ${_photos.length} · agregadas: $_added',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _TabsPlayground extends StatefulWidget {
  const _TabsPlayground({required this.initialIndex, required this.count});

  final int initialIndex;
  final int count;

  @override
  State<_TabsPlayground> createState() => _TabsPlaygroundState();
}

class _TabsPlaygroundState extends State<_TabsPlayground> {
  late int _selected;

  @override
  void initState() {
    super.initState();
    _selected = widget.initialIndex;
  }

  @override
  void didUpdateWidget(covariant _TabsPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialIndex != oldWidget.initialIndex) {
      _selected = widget.initialIndex;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtTabs(
          tabs: <RtTabItem>[
            RtTabItem(text: 'Activos', count: widget.count),
            const RtTabItem(text: 'Historial'),
          ],
          selectedIndex: _selected,
          onSelect: (index) => setState(() => _selected = index),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Pestaña activa: ${_selected == 0 ? 'Activos' : 'Historial'}',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _EmptyStatePlayground extends StatefulWidget {
  const _EmptyStatePlayground({
    required this.icon,
    required this.title,
    required this.message,
    required this.actionLabel,
  });

  final IconData icon;
  final String title;
  final String message;
  final String actionLabel;

  @override
  State<_EmptyStatePlayground> createState() => _EmptyStatePlaygroundState();
}

class _EmptyStatePlaygroundState extends State<_EmptyStatePlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: <Widget>[
        RtEmptyState(
          icon: widget.icon,
          title: widget.title,
          message: widget.message,
          actionLabel: widget.actionLabel.trim().isEmpty
              ? null
              : widget.actionLabel,
          onAction: widget.actionLabel.trim().isEmpty
              ? null
              : () => setState(() => _taps++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Toques en la acción: $_taps',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

class _ItemListEditorPlayground extends StatefulWidget {
  const _ItemListEditorPlayground({
    required this.initialCount,
    required this.listError,
  });

  final int initialCount;
  final String? listError;

  @override
  State<_ItemListEditorPlayground> createState() =>
      _ItemListEditorPlaygroundState();
}

class _ItemListEditorPlaygroundState extends State<_ItemListEditorPlayground> {
  late List<RtOrderItem> _items;

  @override
  void initState() {
    super.initState();
    _items = _buildItems(widget.initialCount);
  }

  @override
  void didUpdateWidget(covariant _ItemListEditorPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialCount != oldWidget.initialCount) {
      _items = _buildItems(widget.initialCount);
    }
  }

  List<RtOrderItem> _buildItems(int count) {
    const samples = <RtOrderItem>[
      RtOrderItem(description: 'Botellas de plástico', weightKg: '2.5'),
      RtOrderItem(description: 'Cartón', weightKg: '12'),
      RtOrderItem(description: 'Periódicos y revistas', weightKg: '5'),
    ];
    return List<RtOrderItem>.generate(
      count,
      (index) => index < samples.length ? samples[index] : const RtOrderItem(),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtItemListEditor(
          items: _items,
          listError: widget.listError,
          onAdd: () => setState(() {
            if (_items.length < RtItemListEditor.maxItems) {
              _items = <RtOrderItem>[..._items, const RtOrderItem()];
            }
          }),
          onRemove: (index) => setState(() {
            if (_items.length > 1) {
              _items = <RtOrderItem>[..._items]..removeAt(index);
            }
          }),
          onChange: (index, description, weightKg) => setState(() {
            _items = <RtOrderItem>[..._items];
            _items[index] = RtOrderItem(
              description: description,
              weightKg: weightKg,
              descriptionError: _items[index].descriptionError,
              weightError: _items[index].weightError,
            );
          }),
        ),
      ],
    );
  }
}

class _OrderCardPlayground extends StatefulWidget {
  const _OrderCardPlayground({
    required this.status,
    required this.acceptances,
    required this.recyclerName,
    required this.showPhoto,
  });

  final RtOrderStatus status;
  final int acceptances;
  final String? recyclerName;
  final bool showPhoto;

  @override
  State<_OrderCardPlayground> createState() => _OrderCardPlaygroundState();
}

class _OrderCardPlaygroundState extends State<_OrderCardPlayground> {
  int _taps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtOrderCard(
          id: 'playground',
          date: 'Hoy, 9:15',
          status: widget.status,
          itemsCount: 3,
          totalWeightKg: 14.5,
          photoUrl: widget.showPhoto ? _photoUrl : null,
          acceptances: widget.acceptances,
          recyclerName: widget.recyclerName,
          onClick: () => setState(() => _taps++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Toques en la tarjeta: $_taps',
          style: Theme.of(context).textTheme.bodyMedium,
        ),
      ],
    );
  }
}

String _photoSizeLabel(RtPhotoThumbSize size) => switch (size) {
  RtPhotoThumbSize.small => 'Pequeño (56)',
  RtPhotoThumbSize.medium => 'Mediano (80)',
  RtPhotoThumbSize.large => 'Grande (112)',
};

IconData _emptyIconByLabel(String label) => switch (label) {
  'Inventario' => Icons.inventory_2_rounded,
  'Buscar' => Icons.search_rounded,
  _ => Icons.recycling_rounded,
};
