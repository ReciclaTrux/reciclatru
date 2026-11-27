# ReciclaTru Flutter

Cliente Flutter de ReciclaTru organizado con las mismas capas de la app Kotlin: `model`,
`view`, `viewmodel`, `domain`, `data`, `di` y `ui/designsystem`. Las pantallas y recorridos
se migran desde el cliente Android; ambas apps consumen la misma API.

La sesión conserva el identificador, el rol y el token con `flutter_secure_storage`.
`SharedPreferences` se reserva para el aviso local de ubicación. Las fotos se procesan al
subirlas: orientación EXIF, lado máximo de 1280 px y ajuste JPEG desde calidad 85 hasta 35
para acercarse al límite de 300 KB definido por Android.

## Ejecutar

```bash
flutter pub get
flutter run
```

Para revisar el análisis estático y las pruebas, ejecuta `flutter analyze` y `flutter test`.
Para compilar el APK, ejecuta `flutter build apk`. La URL de API se define con
`--dart-define=API_BASE_URL=https://reciclatru.onrender.com/api/v1`.

Consulta `AGENTS.md` para las reglas de estructura y migración.
