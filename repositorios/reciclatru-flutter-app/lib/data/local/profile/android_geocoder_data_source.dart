/// Implementa el acceso local de android geocoder data source para persistencia o servicios del dispositivo.
import 'package:flutter/services.dart';

import '../../../model/profile/geo_point.dart';

/// Puente a `android.location.Geocoder`, equivalente del origen Kotlin.
/// El geocodificador es del sistema, no usa API key ni envía el token de sesión.
class AndroidGeocoderDataSource {
  static const MethodChannel _channel = MethodChannel('pe.edu.upn.reciclatru/geocoder');

  /// Indica si Android cuenta con geocodificación instalada.
  Future<bool> isPresent() async =>
      await _channel.invokeMethod<bool>('isPresent') ?? false;

  /// Resuelve la primera coincidencia con el sesgo local que aplica Kotlin.
  Future<GeoPoint?> search(String query) async {
    final raw = await _channel.invokeMapMethod<String, dynamic>(
      'search', {'query': _withTrujilloBias(query)},
    );
    return raw == null ? null : _point(raw);
  }

  /// Obtiene hasta cuatro etiquetas únicas de dirección y distrito.
  Future<List<({String label, GeoPoint point})>> suggest(String query) async {
    final raw = await _channel.invokeListMethod<dynamic>(
      'suggest', {'query': _withTrujilloBias(query)},
    );
    return (raw ?? const <dynamic>[]).whereType<Map>().map((value) {
      final item = Map<String, dynamic>.from(value);
      return (label: item['label']?.toString() ?? '', point: _point(item));
    }).where((item) => item.label.isNotEmpty).toList();
  }

  /// Lee la dirección asociada al pin confirmado por la persona.
  Future<({String address, String district})?> describe(GeoPoint point) async {
    final raw = await _channel.invokeMapMethod<String, dynamic>('describe', {
      'latitude': point.latitude,
      'longitude': point.longitude,
    });
    if (raw == null) return null;
    return (
      address: raw['address']?.toString() ?? '',
      district: raw['district']?.toString() ?? '',
    );
  }

  /// Agrega Trujillo si el texto no especifica la ciudad.
  String _withTrujilloBias(String query) => query.toLowerCase().contains('trujillo')
      ? query
      : '$query, Trujillo, Perú';

  /// Convierte las coordenadas recibidas por el canal a un modelo de dominio.
  GeoPoint _point(Map<String, dynamic> value) => GeoPoint(
    latitude: (value['latitude'] as num).toDouble(),
    longitude: (value['longitude'] as num).toDouble(),
  );
}
