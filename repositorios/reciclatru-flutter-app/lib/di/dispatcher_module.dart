/// En Dart/Flutter las operaciones de Dio y MethodChannel son asíncronas y no
/// requieren inyección de `CoroutineDispatcher` como en `DispatcherModule.kt`.
/// Este archivo conserva la ubicación equivalente sin agregar dispatchers ficticios.
