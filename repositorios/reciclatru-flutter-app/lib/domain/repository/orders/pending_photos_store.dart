// Este contrato guarda temporalmente la lista de imágenes seleccionadas antes de subirlas al servidor.
abstract interface class PendingPhotosStore {
  List<String> get(String orderId);
  void put(String orderId, List<String> photos);
}
