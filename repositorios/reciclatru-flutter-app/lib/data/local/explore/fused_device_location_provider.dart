/// Consulta la ubicación del teléfono sin guardar las coordenadas en la app.
import 'dart:async';

import 'package:geolocator/geolocator.dart';

import '../../../domain/repository/explore/device_location_provider.dart';
import '../../../model/explore/available_order.dart';

/// Equivalente Flutter de FusedDeviceLocationProvider.kt.
/// Solo consulta la ubicación durante la búsqueda; no la persiste.
class FusedDeviceLocationProvider implements DeviceLocationProvider {
  @override
  Future<GeoPoint?> currentLocation() async {
    try {
      if (!await Geolocator.isLocationServiceEnabled()) return null;

      final permission = await Geolocator.checkPermission();
      if (permission != LocationPermission.whileInUse &&
          permission != LocationPermission.always) {
        return null;
      }

      // La ubicación solo calcula la distancia; si demora, usamos la última conocida.
      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(accuracy: LocationAccuracy.low),
      ).timeout(const Duration(seconds: 15));
      return _toGeoPoint(position);
    } on TimeoutException {
      return _lastKnownLocation();
    } on LocationServiceDisabledException {
      return null;
    } on PermissionDeniedException {
      return null;
    } catch (_) {
      // Una falla de ubicación no debe impedir abrir un pedido.
      return _lastKnownLocation();
    }
  }

  /// Usa la posición guardada por el dispositivo cuando no obtiene una actual.
  Future<GeoPoint?> _lastKnownLocation() async {
    try {
      final position = await Geolocator.getLastKnownPosition()
          .timeout(const Duration(seconds: 3));
      return position == null ? null : _toGeoPoint(position);
    } catch (_) {
      return null;
    }
  }

  /// Convierte la posición de Geolocator al modelo compartido por la exploración.
  GeoPoint _toGeoPoint(Position position) => GeoPoint(
        latitude: position.latitude,
        longitude: position.longitude,
      );
}
