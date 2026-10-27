import 'package:flutter/material.dart';
import 'package:maplibre_gl/maplibre_gl.dart';

import '../atoms/rt_button.dart';
import '../molecules/rt_search_field.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Punto geográfico del pedido; el mapa siempre parte de este centro.
class RtLatLng {
  const RtLatLng({required this.latitude, required this.longitude});

  final double latitude;
  final double longitude;

  @override
  bool operator ==(Object other) {
    return other is RtLatLng &&
        other.latitude == latitude &&
        other.longitude == longitude;
  }

  @override
  int get hashCode => Object.hash(latitude, longitude);
}

/// Estilo fijo del Release 1: `positron` de OpenFreeMap. Sin API key ni cuenta.
const String _mapStyleUrl = 'https://tiles.openfreemap.org/styles/positron';

/// Zoom inicial: una calle de Trujillo se reconoce sin perder el barrio.
const double _initialZoom = 16;

/// Créditos de los datos del mapa, obligatorios con OpenStreetMap y OpenFreeMap.
const String _mapAttribution = '© OpenStreetMap · OpenFreeMap';

/// Mapa con pin fijo al centro, buscador arriba y botón Confirmar ubicación abajo.
///
/// El mapa se mueve debajo del pin; sin GPS ni permiso de ubicación. El mapa lo
/// pinta MapLibre con el estilo `positron` de OpenFreeMap.
class RtMapPicker extends StatefulWidget {
  const RtMapPicker({
    super.key,
    required this.initialCenter,
    required this.onConfirm,
    this.center,
    this.searchEnabled = true,
    this.onSearch,
    this.onSearchQueryChanged,
    this.searchSuggestions = const <String>[],
    this.searchLoading = false,
    this.onSuggestionSelected,
    this.searchError,
  });

  final RtLatLng initialCenter;

  /// Centro solicitado por la feature, por ejemplo el resultado del geocoder.
  ///
  /// Cuando cambia, el mapa anima la cámara. El organismo no resuelve
  /// direcciones: eso vive en la feature (HU-04).
  final RtLatLng? center;

  /// Entrega la latitud y la longitud del pin.
  final void Function(double latitude, double longitude) onConfirm;

  /// Muestra el buscador; la pantalla lo apaga si no hay geocoder.
  final bool searchEnabled;

  /// Avisa el texto buscado al confirmar desde el teclado.
  final ValueChanged<String>? onSearch;

  /// Avisa cada cambio del texto para que la feature haga el debounce, consulte
  /// al geocoder y entregue [searchSuggestions]. El organismo no debounce ni
  /// conoce el geocoder.
  final ValueChanged<String>? onSearchQueryChanged;

  /// Direcciones sugeridas por la feature; el buscador muestra hasta cuatro.
  final List<String> searchSuggestions;

  /// Muestra el spinner del buscador mientras la feature consulta al geocoder.
  final bool searchLoading;

  /// Se llama al elegir una sugerencia; la feature resuelve y actualiza [center].
  final ValueChanged<String>? onSuggestionSelected;

  /// Error del buscador, por ejemplo "No encontramos esa dirección. Mueve el mapa a mano.".
  final String? searchError;

  @override
  State<RtMapPicker> createState() => _RtMapPickerState();
}

class _RtMapPickerState extends State<RtMapPicker> {
  MapLibreMapController? _controller;
  LatLng? _center;
  String _query = '';

  @override
  void initState() {
    super.initState();
    // Debe asignarse antes de que se construya el primer MapLibreMap.
    MapLibreMap.useHybridComposition = true;
  }

  RtLatLng get _currentCenter {
    final target = _controller?.cameraPosition?.target ?? _center;
    if (target == null) {
      return widget.center ?? widget.initialCenter;
    }
    return RtLatLng(latitude: target.latitude, longitude: target.longitude);
  }

  @override
  void didUpdateWidget(covariant RtMapPicker oldWidget) {
    super.didUpdateWidget(oldWidget);
    final center = widget.center;
    if (center != null && center != oldWidget.center) {
      final target = LatLng(center.latitude, center.longitude);
      _center = target;
      _controller?.animateCamera(
        CameraUpdate.newLatLngZoom(target, _initialZoom),
        duration: const Duration(milliseconds: 500),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final radius = BorderRadius.circular(RtRadii.lg);

    return Stack(
      children: <Widget>[
        Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: <Widget>[
            if (widget.searchEnabled)
              const SizedBox(height: RtSize.inputHeight + RtSpacing.s3),
            Container(
              height: RtSize.mapHeight,
              decoration: BoxDecoration(
                color: colors.surfaceContainerHighest,
                borderRadius: radius,
                border: Border.all(
                  color: colors.outline,
                  width: RtStroke.control,
                ),
              ),
              clipBehavior: Clip.antiAlias,
              child: Stack(
                children: <Widget>[
                  Positioned.fill(
                    child: MapLibreMap(
                      styleString: _mapStyleUrl,
                      initialCameraPosition: CameraPosition(
                        target: LatLng(
                          (widget.center ?? widget.initialCenter).latitude,
                          (widget.center ?? widget.initialCenter).longitude,
                        ),
                        zoom: _initialZoom,
                      ),
                      rotateGesturesEnabled: false,
                      tiltGesturesEnabled: false,
                      compassEnabled: false,
                      logoEnabled: false,
                      attributionButtonPosition:
                          AttributionButtonPosition.bottomLeft,
                      trackCameraPosition: true,
                      onMapCreated: (controller) {
                        _controller = controller;
                        final center = widget.center;
                        if (center != null) {
                          controller.animateCamera(
                            CameraUpdate.newLatLngZoom(
                              LatLng(center.latitude, center.longitude),
                              _initialZoom,
                            ),
                            duration: const Duration(milliseconds: 500),
                          );
                        }
                      },
                      onCameraMove: (position) {
                        _center = position.target;
                      },
                      onMapClick: (_, _) {
                        FocusManager.instance.primaryFocus?.unfocus();
                        if (_query.isEmpty) {
                          return;
                        }
                        setState(() => _query = '');
                        widget.onSearchQueryChanged?.call('');
                      },
                    ),
                  ),
                  Align(
                    alignment: Alignment.center,
                    child: Transform.translate(
                      offset: const Offset(0, -RtSize.iconMd),
                      child: Icon(
                        Icons.location_on_rounded,
                        size: RtSize.mapPin,
                        color: colors.primary,
                        semanticLabel: 'Punto de recojo',
                      ),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: RtSpacing.s3),
            Container(
              padding: const EdgeInsets.all(RtSpacing.s4),
              decoration: BoxDecoration(
                color: colors.surface,
                borderRadius: radius,
                boxShadow: RtElevation.level3,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: <Widget>[
                  Text(
                    'Mueve el mapa hasta tu puerta',
                    style: theme.textTheme.bodyLarge!.copyWith(
                      fontWeight: FontWeight.w600,
                      color: colors.onSurface,
                    ),
                  ),
                  const SizedBox(height: RtSpacing.s1),
                  Text(
                    'El pin marca el punto donde el reciclador recogerá tu '
                    'material. No usamos tu GPS.',
                    style: theme.textTheme.bodyMedium!.copyWith(
                      color: colors.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: RtSpacing.s3),
                  RtButton(
                    text: 'Confirmar ubicación',
                    fullWidth: true,
                    onClick: () {
                      final center = _currentCenter;
                      widget.onConfirm(center.latitude, center.longitude);
                    },
                  ),
                  const SizedBox(height: RtSpacing.s2),
                  Text(
                    _mapAttribution,
                    style: theme.textTheme.labelMedium!.copyWith(
                      color: colors.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        if (widget.searchEnabled)
          Positioned(
            top: 0,
            left: 0,
            right: 0,
            child: RtSearchField(
              query: _query,
              onQueryChange: (value) {
                setState(() => _query = value);
                widget.onSearchQueryChanged?.call(value);
              },
              onSearch: () => widget.onSearch?.call(_query),
              error: widget.searchError,
              suggestions: widget.searchSuggestions,
              suggestionsLoading: widget.searchLoading,
              onSuggestionSelected: (suggestion) {
                // El buscador es un atajo: se limpia y el pin manda.
                setState(() => _query = '');
                widget.onSuggestionSelected?.call(suggestion);
              },
            ),
          ),
      ],
    );
  }
}
