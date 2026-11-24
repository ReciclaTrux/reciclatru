/// Formato de datos que el sistema muestra igual en varias pantallas
/// (`README.md`, "Voz y contenido").
abstract final class RtFormat {
  /// Peso en kilogramos con punto decimal y un decimal como máximo:
  /// "14.5 kg", "6 kg".
  static String kg(double value) {
    final rounded = (value * 10).round() / 10;
    final text = rounded == rounded.roundToDouble()
        ? rounded.toStringAsFixed(0)
        : rounded.toStringAsFixed(1);
    return '$text kg';
  }

  /// Distancia: "a 800 m" si es menor a 1 km; si no, un decimal: "a 1.2 km".
  static String distanceKm(double value) {
    if (value < 1.0) {
      return 'a ${(value * 1000).round()} m';
    }
    return 'a ${value.toStringAsFixed(1)} km';
  }

  /// Cantidad de ítems: "1 ítem", "3 ítems".
  static String items(int count) => count == 1 ? '1 ítem' : '$count ítems';

  /// Agrupa un celular de nueve dígitos en tres: "987 654 321". Si el texto
  /// no es un celular, lo devuelve igual.
  static String phone(String value) {
    final digits = value.replaceAll(RegExp(r'[^0-9]'), '');
    final isPhone = digits.length == 9 && !value.contains(RegExp(r'[a-zA-Z]'));
    if (!isPhone) {
      return value;
    }
    return '${digits.substring(0, 3)} ${digits.substring(3, 6)} '
        '${digits.substring(6)}';
  }
}
