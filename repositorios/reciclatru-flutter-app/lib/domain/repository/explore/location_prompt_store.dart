// Este contrato guarda el estado del aviso de ubicación que se muestra al reciclador.
abstract interface class LocationPromptStore {
  Future<bool> wasShown();
  Future<void> markShown();
}
