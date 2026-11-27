/// Resultado de la comprobación de conexión del backend.
class HealthUiState {
  const HealthUiState({this.checking = true, this.connected = false, this.error});
  final bool checking;
  final bool connected;
  final String? error;
}
