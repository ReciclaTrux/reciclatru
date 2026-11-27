/// Implementa el repositorio geocoding repository impl conectando los contratos del dominio con sus fuentes de datos.
import '../../local/profile/android_geocoder_data_source.dart';
import 'package:flutter/services.dart';
import '../../../domain/repository/profile/geocoding_repository.dart';
import '../../../model/profile/geo_point.dart';
import '../../../model/profile/pickup_location.dart';

/// Traduce el geocodificador del sistema al contrato de dominio.
class GeocodingRepositoryImpl implements GeocodingRepository {
  GeocodingRepositoryImpl(this._dataSource);
  final AndroidGeocoderDataSource _dataSource;

  @override
  bool get isAvailable => _isAvailable;

  // Se consulta de forma asíncrona al inicializar el ViewModel.
  bool _isAvailable = false;

  /// Carga la disponibilidad del servicio nativo, sin solicitar permisos.
  Future<void> initialize() async {
    try {
      _isAvailable = await _dataSource.isPresent();
    } on PlatformException {
      _isAvailable = false;
    }
  }

  @override
  Future<List<AddressSuggestion>> suggestAddresses(String query) async {
    if (query.trim().isEmpty) return const [];
    try {
      final rows = await _dataSource.suggest(query.trim());
      return rows.map((row) => AddressSuggestion(label: row.label, point: row.point)).toList();
    } on PlatformException catch (error) {
      throw GeocodingException(error.message ?? 'No pudimos buscar esa dirección.');
    }
  }

  @override
  Future<GeoPoint?> searchAddress(String query) async {
    if (query.trim().isEmpty) return null;
    try {
      return await _dataSource.search(query.trim());
    } on PlatformException catch (error) {
      throw GeocodingException(error.message ?? 'No pudimos buscar esa dirección.');
    }
  }

  @override
  Future<PickupAddress?> describePoint(GeoPoint point) async {
    try {
      final result = await _dataSource.describe(point);
      return result == null ? null : PickupAddress(address: result.address, district: result.district);
    } on PlatformException catch (error) {
      throw GeocodingException(error.message ?? 'No pudimos leer la dirección del pin.');
    }
  }
}

/// Error localizado devuelto por el servicio nativo.
class GeocodingException implements Exception {
  const GeocodingException(this.message);
  final String message;
  @override
  String toString() => message;
}
