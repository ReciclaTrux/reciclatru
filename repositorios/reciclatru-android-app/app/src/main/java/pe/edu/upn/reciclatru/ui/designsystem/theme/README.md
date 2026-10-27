# theme

Tokens del sistema de diseño (https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2): colores, tipografía (Nunito Sans), espaciado,
formas y tamaños. La fuente es `project/tokens.json`; el mapeo a Material 3 y la extensión
propia están en `project/implementacion.md`.

| Archivo | Contenido |
| --- | --- |
| `RtColors.kt` | Esquema de color de Material 3 (`RtLightColorScheme`) y `RtExtraColors` con lo que Material no tiene: `ods-12`, `primary-pressed`, `secondary-pressed`, `ink-subtle`, `warning-*`, `info-*`, `status-*`, `disabled-*`. Se accede con `MaterialTheme.rtColors`. |
| `RtTypography.kt` | Nunito Sans 400/600/700 con `ui-text-google-fonts` y la escala de `implementacion.md`; `RtTextStyles` para `body-strong`, `button`, `button` compacto e iniciales del avatar. |
| `RtSpacing.kt` | `RtSpacing` (space-1 a space-7), `RtSize` (familia `size`), `RtRadius` y `RtStroke`. |
| `RtShapes.kt` | `RtShapes`: `radius-sm`, `radius-md`, `radius-lg`, `radius-full`. |
| `RtFocus.kt` | Anillo de foco de 2 px en `primary-pressed` con 2 px de separación. |
| `RtTheme.kt` | `RtTheme`: único tema de la app. Tema claro, sin color dinámico. |

Implementado en el lote 1. Nada fuera de esta carpeta define colores, tamaños ni radios
literales.
