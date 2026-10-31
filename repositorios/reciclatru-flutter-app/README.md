# ReciclaTru — Flutter

Cliente iOS/multiplataforma de **ReciclaTru**, que conecta a vecinos de Trujillo con
recicladores (ODS 12). Flutter + Material 3, **Riverpod** para estado e inyección y
**go_router** para navegación. Comparte con la app Android los mismos dominios y los mismos
componentes `Rt*`.

## Estructura

```text
lib/
├── main.dart
├── core/
│   ├── design_system/   # sistema de diseño: theme/ atoms/ molecules/ organisms/
│   ├── network/  router/  session/
└── features/            # auth, profile, orders, explore, tracking (presentation/ domain/ data/)
```

## Sistema de diseño

La interfaz usa **solo** el sistema de diseño de ReciclaTru (Atomic Design, componentes
`Rt*` compartidos con la app Android):
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

No se crean componentes fuera de ese catálogo. Detalle y estado por HU en
[`lib/core/design_system/README.md`](lib/core/design_system/README.md#componentes-por-hu).

## Comandos

```bash
flutter pub get
flutter run
flutter analyze
flutter test
```

## Conexión con el backend (RT-49)

Al abrir la app, la pantalla de inicio llama a `GET /api/v1/health` y muestra
**"Conectado"** si la API responde. Si falla, muestra el error con **Reintentar**. La
llamada usa `healthApiProvider` (`lib/core/network/health_api.dart`) y el estado vive en
`backendStatusProvider` (`lib/core/network/backend_status_notifier.dart`).

### Problemas encontrados y cómo quedaron resueltos

- **URL**: por defecto la app apuntaba a `http://localhost:3000`, que en el emulador de
  Android es el propio emulador, y sin el prefijo `/api/v1`. Ahora el valor por defecto
  de `API_BASE_URL` (`lib/core/network/api_config.dart`) es la API publicada:
  `https://reciclatru.onrender.com/api/v1`. Para otra URL no se toca el código:
  `flutter run --dart-define=API_BASE_URL=<url>`.
- **Backend local en el emulador de Android**: la PC se llama `10.0.2.2`, no `localhost`
  (`--dart-define=API_BASE_URL=http://10.0.2.2:3000/api/v1`). Como es HTTP sin cifrar,
  Android 9+ lo bloquea salvo que se permita el tráfico cleartext; con la API publicada
  (HTTPS) no hace falta.
- **Permiso de internet**: `android.permission.INTERNET` está en
  `android/app/src/main/AndroidManifest.xml` (y en los manifiestos de `debug` y
  `profile`). Sin él, la app compilada en release no puede llamar a la API.
- **CORS**: solo afecta a Flutter web (Chrome/Edge). Se resolvió en la API
  (`reciclatru-api/src/common/http/cors.ts`); en Android e iOS no aplica.
- **Render en plan gratuito**: el servidor se duerme tras un rato sin uso y la primera
  petición puede tardar cerca de un minuto. `dioProvider` espera hasta 60 s; si aun así
  falla, usar Reintentar.

### Cómo probarlo

1. Abrir `https://reciclatru.onrender.com/api/v1/health` en el navegador: debe responder
   `{"status":"UP",...}`.
2. Con el emulador abierto: `flutter run`. El inicio debe mostrar "Conectado".
3. Con el emulador en modo avión, debe mostrar el error con Reintentar.

## Más información

Reglas del proyecto y de arquitectura: [`AGENTS.md`](AGENTS.md).
