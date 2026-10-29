# feature/profile

**Dominio:** perfil del usuario — datos propios, de contacto y punto de recojo.

| HU | Estado |
| --- | --- |
| HU-03 (completar perfil) | pendiente |
| HU-04 (punto de recojo) | implementada como ejemplo de corte vertical |

HU-04 usa `RtMapPicker`, `RtSearchField`, `RtTextField`, `RtInlineAlert` y `RtActionBar`.
El mapa es MapLibre con el estilo `positron` de OpenFreeMap y el geocoder es el de Android
(con autocomplete y debounce de 300 ms); la ubicación se guarda en la API y requiere sesión.

- `ui/` — pantallas, ViewModel y estado.
- `domain/` — modelos, casos de uso y contratos de repositorio.
- `data/` — API, DTOs, mappers, Geocoder e implementaciones.
