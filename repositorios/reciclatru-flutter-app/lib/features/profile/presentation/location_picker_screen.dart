import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../../core/design_system/organisms/rt_map_picker.dart';
import '../../../core/design_system/organisms/rt_top_app_bar.dart';
import '../../../core/design_system/theme/rt_radii.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import '../domain/user_profile.dart';

/// P06: Marcar ubicación en el mapa (HU-04).
/// Utiliza el organismo oficial RtMapPicker del sistema de diseño.
class LocationPickerScreen extends StatefulWidget {
  const LocationPickerScreen({super.key, this.initialPoint});

  final PickupAddress? initialPoint;

  @override
  State<LocationPickerScreen> createState() => _LocationPickerScreenState();
}

class _LocationPickerScreenState extends State<LocationPickerScreen> {
  static const _defaultCenter = RtLatLng(latitude: -8.1116, longitude: -79.0287);

  final _dio = Dio();
  late RtLatLng _initialCenter;
  RtLatLng? _currentCenter;

  bool _searching = false;
  String? _searchError;
  List<Map<String, dynamic>> _searchResults = [];

  @override
  void initState() {
    super.initState();
    _initialCenter = widget.initialPoint == null
        ? _defaultCenter
        : RtLatLng(
            latitude: widget.initialPoint!.latitude,
            longitude: widget.initialPoint!.longitude,
          );
  }

  Future<void> _searchLocation(String query) async {
    final text = query.trim();
    if (text.isEmpty || _searching) return;

    setState(() {
      _searching = true;
      _searchError = null;
    });

    try {
      final response = await _dio.get<dynamic>(
        'https://nominatim.openstreetmap.org/search',
        queryParameters: {
          'q': text,
          'format': 'jsonv2',
          'limit': 4,
          'countrycodes': 'pe',
          'accept-language': 'es',
        },
        options: Options(
          headers: {'User-Agent': 'ReciclaTru-Flutter/1.0'},
          receiveTimeout: const Duration(seconds: 10),
        ),
      );

      final list = response.data as List;
      if (list.isEmpty) {
        setState(() {
          _searchError = 'No encontramos esa dirección. Mueve el mapa a mano.';
          _searchResults = [];
        });
        return;
      }

      setState(() {
        _searchResults = list.map((e) => Map<String, dynamic>.from(e as Map)).toList();
      });

      // Si sólo hay un resultado o viene de enter/submit, centrar en la primera coincidencia
      final first = _searchResults.first;
      final lat = double.parse(first['lat'].toString());
      final lon = double.parse(first['lon'].toString());
      setState(() {
        _currentCenter = RtLatLng(latitude: lat, longitude: lon);
      });
    } catch (_) {
      if (!mounted) return;
      setState(() {
        _searchError = 'No se pudo buscar la dirección. Inténtalo nuevamente.';
      });
    } finally {
      if (mounted) setState(() => _searching = false);
    }
  }

  void _onSuggestionSelected(String label) {
    final match = _searchResults.firstWhere(
      (element) => (element['display_name'] ?? '').toString() == label,
      orElse: () => _searchResults.isNotEmpty ? _searchResults.first : {},
    );
    if (match.containsKey('lat') && match.containsKey('lon')) {
      final lat = double.parse(match['lat'].toString());
      final lon = double.parse(match['lon'].toString());
      setState(() {
        _currentCenter = RtLatLng(latitude: lat, longitude: lon);
      });
    }
  }

  Future<Map<String, String>> _reverseGeocode(double lat, double lon) async {
    try {
      final response = await _dio.get<dynamic>(
        'https://nominatim.openstreetmap.org/reverse',
        queryParameters: {
          'format': 'jsonv2',
          'lat': lat,
          'lon': lon,
          'zoom': 18,
          'addressdetails': 1,
          'accept-language': 'es',
        },
        options: Options(
          headers: {'User-Agent': 'ReciclaTru-Flutter/1.0'},
          receiveTimeout: const Duration(seconds: 10),
        ),
      );

      final data = Map<String, dynamic>.from(response.data as Map);
      final details = data['address'] is Map
          ? Map<String, dynamic>.from(data['address'] as Map)
          : <String, dynamic>{};

      final road = details['road'] ?? details['pedestrian'] ?? details['suburb'] ?? '';
      final houseNumber = details['house_number'] ?? '';
      final addressText = road.isNotEmpty
          ? (houseNumber.isNotEmpty ? '$road $houseNumber' : road.toString())
          : (data['display_name']?.toString() ?? '');

      final district = (details['city_district'] ??
              details['suburb'] ??
              details['municipality'] ??
              details['city'] ??
              details['town'] ??
              '')
          .toString();

      return {
        'address': addressText,
        'district': district,
      };
    } catch (_) {
      return {'address': '', 'district': ''};
    }
  }

  Future<void> _confirmPoint(double lat, double lon) async {
    final geo = await _reverseGeocode(lat, lon);
    if (!mounted) return;

    final selectedPoint = PickupAddress(
      latitude: double.parse(lat.toStringAsFixed(6)),
      longitude: double.parse(lon.toStringAsFixed(6)),
      address: geo['address'] ?? '',
      district: geo['district'] ?? '',
    );

    final saved = await context.push<PickupAddress>(
      '/pickup-location/confirm',
      extra: selectedPoint,
    );

    if (!mounted) return;
    if (saved != null) {
      context.pop(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(
            title: 'Punto de recojo',
            onBack: () => context.pop(),
          ),
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(RtSpacing.s4),
          child: RtMapPicker(
            key: const Key('map_punto_recojo'),
            initialCenter: _initialCenter,
            center: _currentCenter,
            onConfirm: _confirmPoint,
            searchEnabled: true,
            onSearch: _searchLocation,
            onSearchQueryChanged: (query) {
              if (query.length >= 3) {
                _searchLocation(query);
              }
            },
            searchSuggestions: _searchResults
                .map((e) => (e['display_name'] ?? '').toString())
                .where((s) => s.isNotEmpty)
                .take(4)
                .toList(),
            searchLoading: _searching,
            onSuggestionSelected: _onSuggestionSelected,
            searchError: _searchError,
          ),
        ),
      ),
    );
  }
}
