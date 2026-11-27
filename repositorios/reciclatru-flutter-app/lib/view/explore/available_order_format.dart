/// Formatea el peso en kilogramos con un decimal como máximo.
String formatAvailableWeight(double weightKg) {
  final tenths = (weightKg * 10).round();
  return tenths % 10 == 0 ? '${tenths ~/ 10} kg' : '${tenths ~/ 10}.${tenths % 10} kg';
}

/// Usa la forma singular solo para un material.
String formatAvailableItems(int count) => count == 1 ? '1 ítem' : '$count ítems';

/// Presenta la distancia aproximada o la omite si no hay ubicación.
String? formatDistance(double? distanceKm) {
  if (distanceKm == null) return null;
  final tenths = (distanceKm * 10).round();
  return tenths % 10 == 0 ? 'a ${tenths ~/ 10} km' : 'a ${tenths ~/ 10}.${tenths % 10} km';
}

/// Muestra el tiempo transcurrido desde la publicación.
String? formatTimeAgo(DateTime? createdAt, {DateTime? now}) {
  if (createdAt == null) return null;
  final minutes = (now ?? DateTime.now()).difference(createdAt).inMinutes.clamp(0, 1 << 31);
  final hours = minutes ~/ 60;
  final days = hours ~/ 24;
  if (minutes < 1) return 'ahora';
  if (minutes < 60) return 'hace $minutes min';
  if (hours < 24) return 'hace $hours h';
  if (days < 2) return 'ayer';
  return 'hace $days días';
}
