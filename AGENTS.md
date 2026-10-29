# AGENTS.md — ReciclaTru (raíz)

## Qué es este proyecto

ReciclaTru conecta a vecinos de Trujillo (Perú) con recicladores. Un vecino publica un
pedido con materiales y pesos; un reciclador lo explora, lo acepta y lo recoge. Es
desarrollada por un equipo universitario de 7 personas y el Release 1 se entrega el
**26 de noviembre de 2026**.

Es un monorepo con dos clientes que usan el mismo backend. Todo vive bajo `repositorios/`:

| Carpeta                                  | Tecnología                                                    | Rol                              |
| ---------------------------------------- | ------------------------------------------------------------- | -------------------------------- |
| `repositorios/reciclatru-android-app/`   | Kotlin + Jetpack Compose + Material 3, MVVM + Hilt             | Cliente principal                |
| `repositorios/reciclatru-flutter-app/`   | Flutter + Material 3, Riverpod + go_router                     | Cliente iOS (se trabaja después) |
| `repositorios/reciclatru-openapi/`       | Contrato OpenAPI (aún vacío)                                   | Contrato de la API               |
| `repositorios/reciclatru-arquitectura/`  | C4 en Mermaid (`reciclatru-c4-architecture.md`) y SVG          | Documentación de arquitectura    |
| `repositorios/reciclatru-data-model/`    | Modelo de datos (`reciclatru-data-model.md`)                   | Documentación del modelo         |
| `repositorios/reciclatru-ux/`            | Diseño UX (aún vacío)                                          | Documentación de UX              |

Ambos clientes consumen **la misma API**. Cada historia de usuario (HU) es un **corte
vertical**: pantalla + consumo de la API, demostrable de punta a punta.

## Reglas por proyecto

Cada cliente tiene su propio `AGENTS.md` con las reglas de su stack: revisa el de
`repositorios/reciclatru-android-app/` o el de `repositorios/reciclatru-flutter-app/`
antes de tocar código en esa carpeta.

Principios generales que aplican a ambos clientes:

1. **HU como corte vertical**: cada HU se entrega completa (pantalla + consumo de la API),
   en su rama y con un único PR.
2. **Arquitectura por feature**: los dominios `auth`, `profile`, `orders`, `explore` y
   `tracking` se llaman igual en Android y Flutter, con capas `ui`/`presentation`,
   `domain` y `data`.
3. **Atomic Design en ambos clientes**: tema (tokens), átomos, moléculas y organismos;
   prefijo `Rt`, componentes stateless y los mismos nombres en Android y
   Flutter.
4. **Privacidad por diseño**: los clientes no muestran ni guardan teléfono, dirección ni
   coordenadas más allá de lo que la API entrega según el rol y el estado del pedido.
   Secretos fuera del repositorio.
5. **Pruebas con propósito**: en los clientes, tests de ViewModel/provider y de UI con
   dependencias falsas.
6. **Simplicidad**: solo lo que está en el release; prohibido Firebase, Google Maps API
   y cualquier servicio de pago o con API key.
7. **Flujo y trazabilidad**: rama por HU, PR con la clave de Jira, CI en verde y
   aprobación de un responsable técnico.

## Estructura del monorepo

```text
.
├── AGENTS.md                    # instrucciones para agentes (raíz)
└── repositorios/
    ├── reciclatru-android-app/  # app Android (Kotlin, Compose, MVVM + Hilt)
    ├── reciclatru-flutter-app/  # app Flutter (Riverpod + go_router)
    ├── reciclatru-openapi/      # contrato OpenAPI (pendiente)
    ├── reciclatru-arquitectura/ # C4 en Mermaid y diagrama SVG
    ├── reciclatru-data-model/   # modelo de datos
    └── reciclatru-ux/           # diseño UX (pendiente)
```

Android y Flutter tienen su propio `AGENTS.md` con las reglas de su stack.

## Flujo de trabajo

- **Rama por HU**: `feature/SCRUM-XX-descripcion-corta` (ej.
  `feature/SCRUM-12-registro`). La rama es de la HU, no de la persona.
- **Commits convencionales** (`feat`, `fix`, `chore`, `docs`, `test`) e incluir la clave
  de Jira en el mensaje, por ejemplo:
  `feat(orders): SCRUM-14 permitir publicar pedido`.
- **Un PR por HU**, con la clave `SCRUM-XX` en el título y los tests que correspondan. Nadie sube cambios directos a `main`.
- Antes de pedir revisión: compilar, pasar tests y lint del proyecto tocado (ver el
  `AGENTS.md` de cada carpeta). Un PR con CI en rojo no se integra.
- La documentación y los artefactos se escriben en **español**.

## Reglas para agentes de IA

- **Antes de modificar archivos dentro de `repositorios/reciclatru-android-app/`,
  `repositorios/reciclatru-flutter-app/`, lee el `AGENTS.md` de esa carpeta y sigue sus
  reglas.**
- No agregar dependencias, plugins ni servicios que impliquen costo, tarjeta o API key.
- No inventar reglas de negocio: si un criterio de aceptación no está claro, preguntar o
  revisar la HU en Jira antes de implementar.
- Respetar la estructura y los nombres ya existentes; antes de crear un componente,
  verificar si ya existe uno equivalente.
- Nunca incluir secretos, credenciales ni archivos `.env` en el repositorio.
- No borrar trabajo existente sin entenderlo y sin confirmación.
- Si una decisión afecta la arquitectura, actualizar la documentación de
  `repositorios/reciclatru-arquitectura/` (C4 en Mermaid) y el `AGENTS.md` que corresponda.

## Documentación

- `repositorios/reciclatru-arquitectura/`: diagramas C4 y SVG de arquitectura.
- `repositorios/reciclatru-data-model/`: modelo de datos.
- `repositorios/reciclatru-openapi/`: contrato OpenAPI de la API que consumen los clientes.
- `repositorios/reciclatru-android-app/AGENTS.md`,
  `repositorios/reciclatru-flutter-app/AGENTS.md`: reglas de cada cliente.
- Sistema de diseño (Atomic Design): https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2
