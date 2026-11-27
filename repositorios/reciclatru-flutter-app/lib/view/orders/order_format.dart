/// Construye la pantalla o navegación de order format conservando el flujo de usuario de Kotlin.
import '../../model/orders/order.dart';

/// Meses abreviados usados por el formato de fecha de la interfaz.
const _months = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

/// Formatea un peso con un decimal como máximo.
String formatWeightKg(double weightKg) {
  final tenths = (weightKg * 10).round();
  return tenths % 10 == 0 ? '${tenths ~/ 10} kg' : '${tenths ~/ 10}.${tenths % 10} kg';
}

/// Devuelve la forma singular para un único material.
String formatItemCount(int count) => count == 1 ? '1 ítem' : '$count ítems';

/// Formatea la fecha de creación con fecha local peruana.
String formatOrderDate(DateTime? createdAt, {DateTime? now}) =>
    _formatDate(createdAt, prefix: '', now: now, nullLabel: 'Pedido');

/// Formatea la fecha de aceptación.
String formatAcceptedDate(DateTime? acceptedAt, {DateTime? now}) =>
    _formatDate(acceptedAt, prefix: '', now: now, nullLabel: 'aceptó tu pedido');

/// Indica cuándo se publicó el pedido.
String formatPublishedDate(DateTime? createdAt, {DateTime? now}) =>
    _formatDate(createdAt, prefix: 'Publicado', now: now, nullLabel: 'Publicado');

/// Convierte el estado de dominio al texto funcional usado en pantallas.
String formatOrderStatus(OrderStatus status) => switch (status) {
      OrderStatus.pending => 'Pendiente',
      OrderStatus.confirmed => 'Confirmado',
      OrderStatus.collected => 'Recolectado',
      OrderStatus.cancelled => 'Cancelado',
    };

String _formatDate(DateTime? value, {required String prefix, required String nullLabel, DateTime? now}) {
  if (value == null) return nullLabel;
  final local = value.toLocal();
  final current = (now ?? DateTime.now()).toLocal();
  final today = DateTime(current.year, current.month, current.day);
  final date = DateTime(local.year, local.month, local.day);
  final time = '${local.hour}:${local.minute.toString().padLeft(2, '0')}';
  final text = date == today
      ? 'hoy, $time'
      : date == today.subtract(const Duration(days: 1))
          ? 'ayer, $time'
          : '${local.day} ${_months[local.month - 1]}';
  if (prefix.isEmpty) return text[0].toUpperCase() + text.substring(1);
  if (text.startsWith('hoy')) return '$prefix hoy, $time';
  if (text.startsWith('ayer')) return '$prefix ayer, $time';
  return '$prefix el $text';
}
