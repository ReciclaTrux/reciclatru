import 'dart:async';

import 'package:flutter/material.dart';
import 'package:reciclatru/core/design_system/atoms/rt_button.dart';
import 'package:reciclatru/core/design_system/molecules/rt_confirm_dialog.dart';
import 'package:reciclatru/core/design_system/molecules/rt_contact_row.dart';
import 'package:reciclatru/core/design_system/molecules/rt_error_state.dart';
import 'package:reciclatru/core/design_system/molecules/rt_inline_alert.dart';
import 'package:reciclatru/core/design_system/molecules/rt_role_selector.dart';
import 'package:reciclatru/core/design_system/molecules/rt_search_field.dart';
import 'package:reciclatru/core/design_system/molecules/rt_snackbar.dart';
import 'package:reciclatru/core/design_system/organisms/rt_action_bar.dart';
import 'package:reciclatru/core/design_system/organisms/rt_bottom_nav.dart';
import 'package:reciclatru/core/design_system/organisms/rt_map_picker.dart';
import 'package:reciclatru/core/design_system/organisms/rt_top_app_bar.dart';
import 'package:reciclatru/core/design_system/theme/rt_colors.dart';
import 'package:reciclatru/core/design_system/theme/rt_spacing.dart';
import 'package:widgetbook/widgetbook.dart';

final List<WidgetbookNode> lote2Directories = <WidgetbookNode>[
  WidgetbookFolder(
    name: 'Moléculas',
    isInitiallyExpanded: true,
    children: <WidgetbookNode>[
      WidgetbookComponent(
        name: 'RtRoleSelector',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _RoleSelectorStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildRoleSelectorPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtInlineAlert',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Cuatro tonos',
            builder: (_) => _preview(const _InlineAlertTones()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildInlineAlertPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtSearchField',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _SearchFieldStates()),
          ),
          WidgetbookUseCase(
            name: 'Sugerencias',
            builder: (_) => _preview(const _SearchFieldSuggestionsDemo()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildSearchFieldPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtContactRow',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _ContactRowStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildContactRowPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtConfirmDialog',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _ConfirmDialogStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildConfirmDialogPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtSnackbar',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _SnackbarStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildSnackbarPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtErrorState',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _ErrorStateStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildErrorStatePlayground,
          ),
        ],
      ),
    ],
  ),
  WidgetbookFolder(
    name: 'Organismos',
    isInitiallyExpanded: true,
    children: <WidgetbookNode>[
      WidgetbookComponent(
        name: 'RtTopAppBar',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _TopAppBarStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildTopAppBarPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtBottomNav',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Roles',
            builder: (_) => _preview(const _BottomNavRoles()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildBottomNavPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtMapPicker',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Mapa',
            builder: (_) =>
                _preview(const _MapSearchDemo(initialCenter: _trujilloCenter)),
          ),
          WidgetbookUseCase(
            name: 'Búsqueda resuelta',
            builder: (_) => _preview(
              const _MapSearchDemo(
                initialCenter: _trujilloCenter,
                showSimulationButton: true,
              ),
            ),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildMapPickerPlayground,
          ),
        ],
      ),
      WidgetbookComponent(
        name: 'RtActionBar',
        useCases: <WidgetbookUseCase>[
          WidgetbookUseCase(
            name: 'Estados',
            builder: (_) => _preview(const _ActionBarStates()),
          ),
          WidgetbookUseCase(
            name: 'Interactivo',
            builder: _buildActionBarPlayground,
          ),
        ],
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

Widget _scrimPreview(Widget child) {
  return Container(
    color: rtColorScheme.scrim,
    alignment: Alignment.center,
    padding: const EdgeInsets.all(RtSpacing.s4),
    child: child,
  );
}

// ---------------------------------------------------------------------------
// Estados estáticos
// ---------------------------------------------------------------------------

class _RoleSelectorStates extends StatelessWidget {
  const _RoleSelectorStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtRoleSelector(selected: null, onSelect: _noopRole),
        SizedBox(height: RtSpacing.s5),
        RtRoleSelector(
          selected: RtRole.neighbor,
          onSelect: _noopRole,
          error: 'Elige si eres vecino o reciclador',
        ),
      ],
    );
  }
}

void _noopRole(RtRole role) {}

class _InlineAlertTones extends StatelessWidget {
  const _InlineAlertTones();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtInlineAlert(
          tone: RtInlineAlertTone.error,
          message: 'Correo o contraseña incorrectos.',
        ),
        SizedBox(height: RtSpacing.s3),
        RtInlineAlert(
          tone: RtInlineAlertTone.warning,
          title: 'Juan Pérez podría estar en camino',
          message: 'Avísale antes de cancelar: 987 654 321',
        ),
        SizedBox(height: RtSpacing.s3),
        RtInlineAlert(
          tone: RtInlineAlertTone.info,
          message: 'Activa tu ubicación para ver la distancia',
          actionLabel: 'Activar ubicación',
          onAction: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtInlineAlert(
          tone: RtInlineAlertTone.success,
          message:
              'El vecino marcará el pedido como Recolectado cuando le '
              'entregues el material.',
        ),
      ],
    );
  }
}

void _noop() {}

class _SearchFieldStates extends StatelessWidget {
  const _SearchFieldStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtSearchField(query: '', onQueryChange: _noopText, onSearch: _noop),
        SizedBox(height: RtSpacing.s4),
        RtSearchField(
          query: 'Av. Larco 1250',
          onQueryChange: _noopText,
          onSearch: _noop,
        ),
        SizedBox(height: RtSpacing.s4),
        RtSearchField(
          query: 'Calle que no existe',
          onQueryChange: _noopText,
          onSearch: _noop,
          error: 'No encontramos esa dirección. Mueve el mapa a mano.',
        ),
      ],
    );
  }
}

void _noopText(String value) {}

/// Demo de autocomplete del buscador: simula la feature con debounce.
class _SearchFieldSuggestionsDemo extends StatefulWidget {
  const _SearchFieldSuggestionsDemo();

  @override
  State<_SearchFieldSuggestionsDemo> createState() =>
      _SearchFieldSuggestionsDemoState();
}

class _SearchFieldSuggestionsDemoState
    extends State<_SearchFieldSuggestionsDemo> {
  static const Duration _debounceDuration = Duration(milliseconds: 300);

  Timer? _debounce;
  String _query = '';
  List<String> _suggestions = const <String>[];
  bool _loading = false;
  String? _error;
  String? _selected;

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }

  void _onQueryChanged(String query) {
    _debounce?.cancel();
    setState(() {
      _query = query;
      _loading = true;
      _error = null;
      _suggestions = const <String>[];
    });
    if (query.trim().isEmpty) {
      setState(() => _loading = false);
      return;
    }
    _debounce = Timer(_debounceDuration, () {
      if (!mounted) return;
      final suggestions = _fakeSuggestions(query);
      setState(() {
        _loading = false;
        _suggestions = suggestions;
        _error = suggestions.isEmpty
            ? 'No encontramos esa dirección. Mueve el mapa a mano.'
            : null;
      });
    });
  }

  void _onSubmitted() {
    _debounce?.cancel();
    final suggestions = _fakeSuggestions(_query);
    setState(() {
      _loading = false;
      _suggestions = suggestions;
      _error = suggestions.isEmpty && _query.trim().isNotEmpty
          ? 'No encontramos esa dirección. Mueve el mapa a mano.'
          : null;
    });
  }

  void _onSuggestionSelected(String suggestion) {
    _debounce?.cancel();
    setState(() {
      _query = suggestion;
      _suggestions = const <String>[];
      _loading = false;
      _error = null;
      _selected = suggestion;
    });
  }

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.bodyLarge;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtSearchField(
          query: _query,
          onQueryChange: _onQueryChanged,
          onSearch: _onSubmitted,
          error: _error,
          suggestions: _suggestions,
          suggestionsLoading: _loading,
          onSuggestionSelected: _onSuggestionSelected,
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Toca el buscador y escribe "Larco", "Plaza" o "España"; '
          'la feature simula el geocoder después de 300 ms.',
          style: style,
        ),
        if (_selected != null) Text('Elegido: $_selected', style: style),
      ],
    );
  }
}

class _ContactRowStates extends StatelessWidget {
  const _ContactRowStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtContactRow(
          name: 'Juan Pérez',
          subtitle: '987654321',
          phone: '987654321',
        ),
        SizedBox(height: RtSpacing.s4),
        RtContactRow(
          name: 'María Ríos',
          subtitle: 'Aceptó hoy, 10:42',
          phone: '948120337',
        ),
        SizedBox(height: RtSpacing.s4),
        RtContactRow(name: 'María Ríos', subtitle: 'Esperando confirmación'),
      ],
    );
  }
}

class _ConfirmDialogStates extends StatelessWidget {
  const _ConfirmDialogStates();

  @override
  Widget build(BuildContext context) {
    return _scrimPreview(
      const Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          RtConfirmDialog(
            title: '¿Elegir a Juan Pérez?',
            message:
                'Él recogerá tu pedido. Los demás recicladores quedarán '
                'descartados.',
            confirmLabel: 'Elegir',
            onConfirm: _noop,
            onDismiss: _noop,
          ),
          SizedBox(height: RtSpacing.s4),
          RtConfirmDialog(
            title: '¿Seguro que deseas cancelar?',
            message: 'El pedido pasará a tu historial y no se podrá reactivar.',
            confirmLabel: 'Sí, cancelar',
            onConfirm: _noop,
            onDismiss: _noop,
            destructive: true,
          ),
        ],
      ),
    );
  }
}

class _SnackbarStates extends StatelessWidget {
  const _SnackbarStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtSnackbar(
          message: 'Pedido publicado',
          actionLabel: 'Ver',
          onAction: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtSnackbar(
          message: 'No se pudo subir una foto',
          actionLabel: 'Reintentar',
          onAction: _noop,
        ),
        SizedBox(height: RtSpacing.s3),
        RtSnackbar(message: 'Cambios guardados'),
      ],
    );
  }
}

class _ErrorStateStates extends StatelessWidget {
  const _ErrorStateStates();

  @override
  Widget build(BuildContext context) {
    return const Column(
      children: <Widget>[
        RtErrorState(
          title: 'No pudimos cargar tus pedidos',
          message: 'Revisa tu conexión a internet e inténtalo de nuevo.',
          onRetry: _noop,
        ),
        SizedBox(height: RtSpacing.s5),
        RtErrorState(
          title: 'No pudimos cargar el mapa',
          message:
              'El servicio no respondió. Inténtalo de nuevo en un momento.',
          onRetry: _noop,
          kind: RtErrorKind.serverError,
        ),
      ],
    );
  }
}

class _TopAppBarStates extends StatelessWidget {
  const _TopAppBarStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        const RtTopAppBar(title: 'Mis pedidos'),
        const SizedBox(height: RtSpacing.s4),
        RtTopAppBar(
          title: 'Detalle del pedido',
          onBack: _noop,
          action: RtAppBarAction(
            icon: Icons.edit_rounded,
            contentDescription: 'Editar pedido',
            onClick: _noop,
          ),
        ),
      ],
    );
  }
}

class _BottomNavRoles extends StatelessWidget {
  const _BottomNavRoles();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        const RtBottomNav(
          items: _neighborItems,
          selected: 'orders',
          onSelect: _noopText,
        ),
        const SizedBox(height: RtSpacing.s4),
        const RtBottomNav(
          items: _recyclerItems,
          selected: 'available',
          onSelect: _noopText,
        ),
      ],
    );
  }
}

/// Centro de Trujillo con el que arranca el mapa.
const RtLatLng _trujilloCenter = RtLatLng(
  latitude: -8.1116,
  longitude: -79.0287,
);

/// Direcciones del geocoder falso del catálogo: simula la feature HU-04.
///
/// El organismo no conoce estas direcciones; aquí solo demostramos el contrato
/// `onSearchQueryChanged` -> la feature resuelve -> `searchSuggestions`/`center`.
const Map<String, RtLatLng> _fakeAddresses = <String, RtLatLng>{
  'Av. Larco 1250': RtLatLng(latitude: -8.1122, longitude: -79.0274),
  'Plaza de Armas': RtLatLng(latitude: -8.1119, longitude: -79.0288),
  'Av. España 500': RtLatLng(latitude: -8.1108, longitude: -79.0259),
};

List<String> _fakeSuggestions(String query) {
  final normalized = query.trim().toLowerCase();
  if (normalized.isEmpty) {
    return const <String>[];
  }
  return _fakeAddresses.keys
      .where((label) => label.toLowerCase().contains(normalized))
      .take(4)
      .toList(growable: false);
}

RtLatLng? _fakeGeocode(String label) => _fakeAddresses[label];

/// Demo del lado feature: debounce, geocoder falso y selección.
///
/// La lógica del geocoder no vive en el organismo; este widget solo simula lo
/// que hará la pantalla de HU-04 para poder probar el componente.
class _MapSearchDemo extends StatefulWidget {
  const _MapSearchDemo({
    super.key,
    required this.initialCenter,
    this.searchEnabled = true,
    this.showSimulationButton = false,
  });

  final RtLatLng initialCenter;
  final bool searchEnabled;
  final bool showSimulationButton;

  @override
  State<_MapSearchDemo> createState() => _MapSearchDemoState();
}

class _MapSearchDemoState extends State<_MapSearchDemo> {
  static const Duration _debounceDuration = Duration(milliseconds: 300);

  Timer? _debounce;
  RtLatLng? _center;
  String? _error;
  List<String> _suggestions = const <String>[];
  bool _loading = false;
  bool _updated = false;
  String _status = 'Escribe una dirección; la feature simula el geocoder.';
  String? _confirmed;

  @override
  void dispose() {
    _debounce?.cancel();
    super.dispose();
  }

  void _onQueryChanged(String query) {
    _debounce?.cancel();
    final trimmed = query.trim();
    if (trimmed.isEmpty) {
      setState(() {
        _loading = false;
        _updated = false;
        _error = null;
        _suggestions = const <String>[];
        _status = 'Escribe una dirección; la feature simula el geocoder.';
      });
      return;
    }

    setState(() {
      _loading = true;
      _updated = false;
      _error = null;
      _suggestions = const <String>[];
      _status = 'Buscando "$trimmed"...';
    });

    _debounce = Timer(_debounceDuration, () {
      if (!mounted) return;
      final suggestions = _fakeSuggestions(trimmed);
      setState(() {
        _loading = false;
        _suggestions = suggestions;
        _error = suggestions.isEmpty
            ? 'No encontramos esa dirección. Mueve el mapa a mano.'
            : null;
        _status = suggestions.isEmpty
            ? 'La feature no encontró "$trimmed".'
            : 'La feature encontró ${suggestions.length} sugerencias; elige una.';
      });
    });
  }

  void _onSubmitted(String query) {
    _debounce?.cancel();
    final suggestions = _fakeSuggestions(query);
    setState(() {
      _loading = false;
      _suggestions = suggestions;
      _error = suggestions.isEmpty && query.trim().isNotEmpty
          ? 'No encontramos esa dirección. Mueve el mapa a mano.'
          : null;
      _status = suggestions.isEmpty
          ? (query.trim().isEmpty
                ? 'Escribe una dirección; la feature simula el geocoder.'
                : 'La feature no encontró "$query".')
          : 'La feature encontró ${suggestions.length} sugerencias; elige una.';
    });
  }

  void _onSuggestionSelected(String suggestion) {
    _debounce?.cancel();
    setState(() {
      _center = _fakeGeocode(suggestion);
      _suggestions = const <String>[];
      _loading = false;
      _updated = true;
      _error = null;
      _status = 'La feature actualizó el mapa.';
    });
  }

  void _simulateResult() => _onSuggestionSelected('Av. Larco 1250');

  @override
  Widget build(BuildContext context) {
    final style = Theme.of(context).textTheme.bodyLarge;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtMapPicker(
          initialCenter: widget.initialCenter,
          center: _center,
          searchEnabled: widget.searchEnabled,
          searchError: _error,
          searchSuggestions: _suggestions,
          searchLoading: _loading,
          onSearchQueryChanged: _onQueryChanged,
          onSearch: _onSubmitted,
          onSuggestionSelected: _onSuggestionSelected,
          onConfirm: (latitude, longitude) {
            setState(() => _confirmed = '$latitude, $longitude');
          },
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(_status, style: style),
        Text('Confirmado: ${_confirmed ?? '—'}', style: style),
        if (_updated)
          const RtInlineAlert(
            tone: RtInlineAlertTone.info,
            message: 'Dirección actualizada en el mapa.',
          ),
        if (widget.showSimulationButton)
          Align(
            alignment: Alignment.centerLeft,
            child: RtButton(
              text: 'Simular resultado: Av. Larco 1250',
              onClick: _simulateResult,
            ),
          ),
      ],
    );
  }
}

class _ActionBarStates extends StatelessWidget {
  const _ActionBarStates();

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtActionBar(
          primary: RtButton(
            text: 'Publicar pedido',
            fullWidth: true,
            onClick: _noop,
          ),
        ),
        const SizedBox(height: RtSpacing.s4),
        RtActionBar(
          primary: RtButton(
            text: 'Recolectado',
            fullWidth: true,
            onClick: _noop,
          ),
          secondary: RtButton(
            text: 'Cancelar pedido',
            variant: RtButtonVariant.secondary,
            fullWidth: true,
            onClick: _noop,
          ),
        ),
      ],
    );
  }
}

const List<RtBottomNavItem> _neighborItems = <RtBottomNavItem>[
  RtBottomNavItem(
    icon: Icons.receipt_long_rounded,
    label: 'Mis pedidos',
    route: 'orders',
  ),
  RtBottomNavItem(
    icon: Icons.add_circle_rounded,
    label: 'Publicar',
    route: 'publish',
  ),
  RtBottomNavItem(
    icon: Icons.person_rounded,
    label: 'Perfil',
    route: 'profile',
  ),
];

const List<RtBottomNavItem> _recyclerItems = <RtBottomNavItem>[
  RtBottomNavItem(
    icon: Icons.travel_explore_rounded,
    label: 'Disponibles',
    route: 'available',
  ),
  RtBottomNavItem(
    icon: Icons.assignment_turned_in_rounded,
    label: 'Aceptados',
    route: 'accepted',
  ),
  RtBottomNavItem(
    icon: Icons.person_rounded,
    label: 'Perfil',
    route: 'profile',
  ),
];

// ---------------------------------------------------------------------------
// Casos interactivos (knobs)
// ---------------------------------------------------------------------------

Widget _buildRoleSelectorPlayground(BuildContext context) {
  final selectedLabel = context.knobs.object.dropdown<String>(
    label: 'Selección',
    options: const <String>['Ninguno', 'Vecino', 'Reciclador'],
    initialOption: 'Ninguno',
  );
  final error = context.knobs.string(label: 'Error', initialValue: '');
  final role = switch (selectedLabel) {
    'Vecino' => RtRole.neighbor,
    'Reciclador' => RtRole.recycler,
    _ => null,
  };

  return _preview(
    _RoleSelectorPlayground(
      initialRole: role,
      error: error.isEmpty ? null : error,
    ),
  );
}

Widget _buildInlineAlertPlayground(BuildContext context) {
  final tone = context.knobs.object.dropdown<RtInlineAlertTone>(
    label: 'Tono',
    options: RtInlineAlertTone.values,
    initialOption: RtInlineAlertTone.warning,
    labelBuilder: _toneLabel,
  );
  final title = context.knobs.string(label: 'Título', initialValue: '');
  final message = context.knobs.string(
    label: 'Mensaje',
    initialValue: 'Avísale antes de cancelar: 987 654 321',
  );
  final actionLabel = context.knobs.string(
    label: 'Acción',
    initialValue: 'Activar ubicación',
  );

  return _preview(
    _InlineAlertPlayground(
      tone: tone,
      title: title.isEmpty ? null : title,
      message: message,
      actionLabel: actionLabel.isEmpty ? null : actionLabel,
    ),
  );
}

Widget _buildSearchFieldPlayground(BuildContext context) {
  final initialQuery = context.knobs.string(
    label: 'Consulta inicial',
    initialValue: 'Av. Larco 1250',
  );
  final error = context.knobs.string(label: 'Error', initialValue: '');

  return _preview(
    _SearchFieldPlayground(
      initialQuery: initialQuery,
      error: error.isEmpty ? null : error,
    ),
  );
}

Widget _buildContactRowPlayground(BuildContext context) {
  final name = context.knobs.string(
    label: 'Nombre',
    initialValue: 'Juan Pérez',
  );
  final subtitle = context.knobs.string(
    label: 'Dato secundario',
    initialValue: '987654321',
  );
  final phone = context.knobs.string(
    label: 'Teléfono',
    initialValue: '987654321',
  );

  return _preview(
    _ContactRowPlayground(
      name: name,
      subtitle: subtitle,
      phone: phone.isEmpty ? null : phone,
    ),
  );
}

Widget _buildConfirmDialogPlayground(BuildContext context) {
  final title = context.knobs.string(
    label: 'Título',
    initialValue: '¿Elegir a Juan Pérez?',
  );
  final message = context.knobs.string(
    label: 'Mensaje',
    initialValue:
        'Él recogerá tu pedido. Los demás recicladores quedarán descartados.',
  );
  final confirmLabel = context.knobs.string(
    label: 'Botón confirmar',
    initialValue: 'Elegir',
  );
  final dismissLabel = context.knobs.string(
    label: 'Botón volver',
    initialValue: 'Volver',
  );
  final destructive = context.knobs.boolean(
    label: 'Destructivo',
    initialValue: false,
  );
  final loading = context.knobs.boolean(
    label: 'Confirmando',
    initialValue: false,
  );
  final withNotice = context.knobs.boolean(
    label: 'Con aviso de precaución',
    initialValue: false,
  );

  return _preview(
    _scrimPreview(
      _ConfirmDialogPlayground(
        title: title,
        message: message,
        confirmLabel: confirmLabel,
        dismissLabel: dismissLabel,
        destructive: destructive,
        loading: loading,
        withNotice: withNotice,
      ),
    ),
  );
}

Widget _buildSnackbarPlayground(BuildContext context) {
  final message = context.knobs.string(
    label: 'Mensaje',
    initialValue: 'Pedido publicado',
  );
  final actionLabel = context.knobs.string(
    label: 'Acción',
    initialValue: 'Ver',
  );

  return _SnackbarPlayground(
    message: message,
    actionLabel: actionLabel.isEmpty ? null : actionLabel,
  );
}

Widget _buildErrorStatePlayground(BuildContext context) {
  final title = context.knobs.string(
    label: 'Título',
    initialValue: 'No pudimos cargar tus pedidos',
  );
  final message = context.knobs.string(
    label: 'Mensaje',
    initialValue: 'Revisa tu conexión a internet e inténtalo de nuevo.',
  );
  final kind = context.knobs.object.dropdown<RtErrorKind>(
    label: 'Motivo',
    options: RtErrorKind.values,
    initialOption: RtErrorKind.noConnection,
    labelBuilder: _errorKindLabel,
  );

  return _preview(
    _ErrorStatePlayground(title: title, message: message, kind: kind),
  );
}

Widget _buildTopAppBarPlayground(BuildContext context) {
  final title = context.knobs.string(
    label: 'Título',
    initialValue: 'Detalle del pedido',
  );
  final showBack = context.knobs.boolean(
    label: 'Con volver',
    initialValue: true,
  );
  final showAction = context.knobs.boolean(
    label: 'Con acción',
    initialValue: true,
  );

  return _preview(
    _TopAppBarPlayground(
      title: title,
      showBack: showBack,
      showAction: showAction,
    ),
  );
}

Widget _buildBottomNavPlayground(BuildContext context) {
  final role = context.knobs.object.dropdown<String>(
    label: 'Rol',
    options: const <String>['Vecino', 'Reciclador'],
    initialOption: 'Vecino',
  );

  return _preview(
    _BottomNavPlayground(
      items: role == 'Vecino' ? _neighborItems : _recyclerItems,
    ),
  );
}

Widget _buildMapPickerPlayground(BuildContext context) {
  final latitude = context.knobs.double.input(
    label: 'Latitud',
    initialValue: -8.1116,
  );
  final longitude = context.knobs.double.input(
    label: 'Longitud',
    initialValue: -79.0287,
  );
  final searchEnabled = context.knobs.boolean(
    label: 'Con buscador',
    initialValue: true,
  );

  return _preview(
    _MapSearchDemo(
      key: ValueKey<String>('$latitude-$longitude'),
      initialCenter: RtLatLng(latitude: latitude, longitude: longitude),
      searchEnabled: searchEnabled,
    ),
  );
}

Widget _buildActionBarPlayground(BuildContext context) {
  final primaryLabel = context.knobs.string(
    label: 'Acción principal',
    initialValue: 'Recolectado',
  );
  final showSecondary = context.knobs.boolean(
    label: 'Con secundaria',
    initialValue: true,
  );
  final secondaryLabel = context.knobs.string(
    label: 'Acción secundaria',
    initialValue: 'Cancelar pedido',
  );

  return _preview(
    _ActionBarPlayground(
      primaryLabel: primaryLabel.isEmpty ? 'Acción' : primaryLabel,
      secondaryLabel: showSecondary && secondaryLabel.isNotEmpty
          ? secondaryLabel
          : null,
    ),
  );
}

// ---------------------------------------------------------------------------
// Widgets de los casos interactivos
// ---------------------------------------------------------------------------

class _RoleSelectorPlayground extends StatefulWidget {
  const _RoleSelectorPlayground({required this.initialRole, this.error});

  final RtRole? initialRole;
  final String? error;

  @override
  State<_RoleSelectorPlayground> createState() =>
      _RoleSelectorPlaygroundState();
}

class _RoleSelectorPlaygroundState extends State<_RoleSelectorPlayground> {
  late RtRole? _selected = widget.initialRole;

  @override
  void didUpdateWidget(covariant _RoleSelectorPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialRole != oldWidget.initialRole) {
      _selected = widget.initialRole;
    }
  }

  @override
  Widget build(BuildContext context) {
    return RtRoleSelector(
      selected: _selected,
      onSelect: (role) => setState(() => _selected = role),
      error: widget.error,
    );
  }
}

class _InlineAlertPlayground extends StatefulWidget {
  const _InlineAlertPlayground({
    required this.tone,
    required this.message,
    required this.title,
    required this.actionLabel,
  });

  final RtInlineAlertTone tone;
  final String message;
  final String? title;
  final String? actionLabel;

  @override
  State<_InlineAlertPlayground> createState() => _InlineAlertPlaygroundState();
}

class _InlineAlertPlaygroundState extends State<_InlineAlertPlayground> {
  int _actions = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtInlineAlert(
          tone: widget.tone,
          title: widget.title,
          message: widget.message,
          actionLabel: widget.actionLabel,
          onAction: widget.actionLabel == null
              ? null
              : () => setState(() => _actions++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Acciones: $_actions',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

class _SearchFieldPlayground extends StatefulWidget {
  const _SearchFieldPlayground({required this.initialQuery, this.error});

  final String initialQuery;
  final String? error;

  @override
  State<_SearchFieldPlayground> createState() => _SearchFieldPlaygroundState();
}

class _SearchFieldPlaygroundState extends State<_SearchFieldPlayground> {
  late String _query = widget.initialQuery;
  int _searches = 0;

  @override
  void didUpdateWidget(covariant _SearchFieldPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.initialQuery != oldWidget.initialQuery) {
      _query = widget.initialQuery;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtSearchField(
          query: _query,
          onQueryChange: (value) => setState(() => _query = value),
          onSearch: () => setState(() => _searches++),
          error: widget.error,
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Búsquedas: $_searches',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

class _ContactRowPlayground extends StatefulWidget {
  const _ContactRowPlayground({
    required this.name,
    required this.subtitle,
    required this.phone,
  });

  final String name;
  final String subtitle;
  final String? phone;

  @override
  State<_ContactRowPlayground> createState() => _ContactRowPlaygroundState();
}

class _ContactRowPlaygroundState extends State<_ContactRowPlayground> {
  int _calls = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtContactRow(
          name: widget.name,
          subtitle: widget.subtitle,
          phone: widget.phone,
          onCall: () => setState(() => _calls++),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text('Llamadas: $_calls', style: Theme.of(context).textTheme.bodyLarge),
      ],
    );
  }
}

class _ConfirmDialogPlayground extends StatefulWidget {
  const _ConfirmDialogPlayground({
    required this.title,
    required this.message,
    required this.confirmLabel,
    required this.dismissLabel,
    required this.destructive,
    required this.loading,
    required this.withNotice,
  });

  final String title;
  final String message;
  final String confirmLabel;
  final String dismissLabel;
  final bool destructive;
  final bool loading;
  final bool withNotice;

  @override
  State<_ConfirmDialogPlayground> createState() =>
      _ConfirmDialogPlaygroundState();
}

class _ConfirmDialogPlaygroundState extends State<_ConfirmDialogPlayground> {
  String _result = 'Sin acciones';

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        RtConfirmDialog(
          title: widget.title,
          message: widget.message,
          confirmLabel: widget.confirmLabel,
          dismissLabel: widget.dismissLabel,
          destructive: widget.destructive,
          loading: widget.loading,
          onConfirm: () => setState(() => _result = 'Confirmado'),
          onDismiss: () => setState(() => _result = 'Cancelado'),
          notice: widget.withNotice
              ? const RtInlineAlert(
                  tone: RtInlineAlertTone.warning,
                  title: 'Juan Pérez podría estar en camino',
                  message: 'Avísale antes de cancelar: 987 654 321',
                )
              : null,
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          _result,
          style: Theme.of(context).textTheme.bodyLarge!
              .copyWith(color: Colors.white),
        ),
      ],
    );
  }
}

class _SnackbarPlayground extends StatelessWidget {
  const _SnackbarPlayground({required this.message, this.actionLabel});

  final String message;
  final String? actionLabel;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: rtBackground,
      body: Center(
        child: RtButton(
          text: 'Mostrar snackbar',
          onClick: () => showRtSnackbar(
            context,
            message: message,
            actionLabel: actionLabel,
            onAction: () {},
          ),
        ),
      ),
    );
  }
}

class _ErrorStatePlayground extends StatefulWidget {
  const _ErrorStatePlayground({
    required this.title,
    required this.message,
    required this.kind,
  });

  final String title;
  final String message;
  final RtErrorKind kind;

  @override
  State<_ErrorStatePlayground> createState() => _ErrorStatePlaygroundState();
}

class _ErrorStatePlaygroundState extends State<_ErrorStatePlayground> {
  int _retries = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtErrorState(
          title: widget.title,
          message: widget.message,
          kind: widget.kind,
          onRetry: () => setState(() => _retries++),
        ),
        Text(
          'Reintentos: $_retries',
          textAlign: TextAlign.center,
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

class _TopAppBarPlayground extends StatefulWidget {
  const _TopAppBarPlayground({
    required this.title,
    required this.showBack,
    required this.showAction,
  });

  final String title;
  final bool showBack;
  final bool showAction;

  @override
  State<_TopAppBarPlayground> createState() => _TopAppBarPlaygroundState();
}

class _TopAppBarPlaygroundState extends State<_TopAppBarPlayground> {
  int _backs = 0;
  int _actions = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtTopAppBar(
          title: widget.title,
          onBack: widget.showBack ? () => setState(() => _backs++) : null,
          action: widget.showAction
              ? RtAppBarAction(
                  icon: Icons.edit_rounded,
                  contentDescription: 'Editar pedido',
                  onClick: () => setState(() => _actions++),
                )
              : null,
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Volver: $_backs · Acción: $_actions',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

class _BottomNavPlayground extends StatefulWidget {
  const _BottomNavPlayground({required this.items});

  final List<RtBottomNavItem> items;

  @override
  State<_BottomNavPlayground> createState() => _BottomNavPlaygroundState();
}

class _BottomNavPlaygroundState extends State<_BottomNavPlayground> {
  late String _selected = widget.items.first.route;

  @override
  void didUpdateWidget(covariant _BottomNavPlayground oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (widget.items != oldWidget.items) {
      _selected = widget.items.first.route;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtBottomNav(
          items: widget.items,
          selected: _selected,
          onSelect: (route) => setState(() => _selected = route),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Destino: $_selected',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

class _ActionBarPlayground extends StatefulWidget {
  const _ActionBarPlayground({
    required this.primaryLabel,
    required this.secondaryLabel,
  });

  final String primaryLabel;
  final String? secondaryLabel;

  @override
  State<_ActionBarPlayground> createState() => _ActionBarPlaygroundState();
}

class _ActionBarPlaygroundState extends State<_ActionBarPlayground> {
  int _primaryTaps = 0;
  int _secondaryTaps = 0;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        RtActionBar(
          primary: RtButton(
            text: widget.primaryLabel,
            fullWidth: true,
            onClick: () => setState(() => _primaryTaps++),
          ),
          secondary: widget.secondaryLabel == null
              ? null
              : RtButton(
                  text: widget.secondaryLabel!,
                  variant: RtButtonVariant.secondary,
                  fullWidth: true,
                  onClick: () => setState(() => _secondaryTaps++),
                ),
        ),
        const SizedBox(height: RtSpacing.s3),
        Text(
          'Principal: $_primaryTaps · Secundaria: $_secondaryTaps',
          style: Theme.of(context).textTheme.bodyLarge,
        ),
      ],
    );
  }
}

String _toneLabel(RtInlineAlertTone tone) => switch (tone) {
  RtInlineAlertTone.error => 'Error',
  RtInlineAlertTone.warning => 'Precaución',
  RtInlineAlertTone.info => 'Información',
  RtInlineAlertTone.success => 'Éxito',
};

String _errorKindLabel(RtErrorKind kind) => switch (kind) {
  RtErrorKind.noConnection => 'Sin conexión',
  RtErrorKind.serverError => 'Error del servidor',
};
