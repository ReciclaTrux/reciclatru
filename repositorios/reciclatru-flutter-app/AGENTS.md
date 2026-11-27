# AGENTS.md — Cliente Flutter de ReciclaTru

El cliente Flutter migra el cliente Android Kotlin y conserva la organización de capas que
usa el código fuente. Flutter cambia únicamente el lenguaje y los mecanismos propios de la
plataforma; no agrega destinos ni reglas de negocio que no existan en Android.

## Estructura obligatoria

```text
lib/
├── main.dart
├── model/                         # modelos Kotlin traducidos a Dart
├── view/                          # pantallas; subcarpetas por dominio y navigation/
├── viewmodel/                     # estado y acciones de pantalla
├── domain/
│   ├── repository/                # contratos por dominio
│   └── usecase/                   # casos de uso por dominio
├── data/
│   ├── local/                     # sesión, ubicación y archivos locales
│   ├── remote/                    # API, DTO y mappers
│   └── repository/                # implementaciones de contratos
├── di/                            # providers de dependencias
└── ui/designsystem/               # theme/, atoms/, molecules/, organisms/
```

No crear `core/`, `features/`, `dev/`, capas nuevas ni carpetas por comodidad. Los nombres
que corresponden a carpetas Android se mantienen. Los archivos Dart usan `snake_case.dart`.

## Criterios de migración

- Mantener las mismas pantallas, rutas de usuario, textos funcionales y restricciones que
  existen en `../reciclatru-android-app/app/src/main/java/pe/edu/upn/reciclatru/`.
- No crear pantallas, flujos, estados de pedido, roles ni validaciones que no estén en la
  app Kotlin. El backend conserva la autoridad de permisos y estados.
- Mantener los dominios `auth`, `profile`, `orders`, `explore` y `health` en sus capas.
- Seguir MVVM: `view` representa el estado, `viewmodel` procesa acciones, `domain` define
  contratos/casos de uso y `data` implementa acceso local y remoto.
- Reutilizar los widgets `Rt*` de `ui/designsystem`. Mantener tema, tamaños y colores desde
  sus tokens.
- Documentar en español el propósito de cada archivo y las decisiones no evidentes del
  flujo para que el proyecto sirva como material de aprendizaje.
- No guardar tokens, teléfonos, ubicaciones ni datos del usuario fuera de lo que exige la
  sesión o las operaciones del backend.

## Comandos

Desde esta carpeta: `flutter pub get`, `flutter analyze`, `flutter test` y `flutter build apk`.
