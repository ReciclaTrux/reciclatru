# organisms — Organismos

Componentes de este nivel, **definidos en el sistema de diseño** (https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2).
Cada uno tiene su guía en `project/components/<Nombre>/README.md` (parámetros en "Qué
recibe") y su vista previa con los estados a replicar. No se agregan componentes que no
estén en esta lista.

- `RtAcceptanceCard` → `RtAcceptanceCard.kt` — pendiente (lote 4)
- `RtAcceptedOrderCard` → `RtAcceptedOrderCard.kt` — pendiente (lote 4)
- `RtActionBar` → `RtActionBar.kt` — implementado (lote 2)
- `RtAvailableOrderCard` → `RtAvailableOrderCard.kt` — pendiente (lote 4)
- `RtBottomNav` → `RtBottomNav.kt` — implementado (lote 2)
- `RtItemListEditor` → `RtItemListEditor.kt` — pendiente (lote 3)
- `RtLocationPrompt` → `RtLocationPrompt.kt` — pendiente (lote 4)
- `RtMapPicker` → `RtMapPicker.kt` — implementado (lote 2): mapa MapLibre real, estilo `positron` de OpenFreeMap y `center` opcional para mover la cámara.
- `RtOrderCard` → `RtOrderCard.kt` — pendiente (lote 3)
- `RtPhotoGallery` → `RtPhotoGallery.kt` — pendiente (lote 4)
- `RtTopAppBar` → `RtTopAppBar.kt` — implementado (lote 2)

Se implementan por sprint, antes de que empiece, según las HU que lo usan (inventario por
HU en el sistema). Orden: tema → átomos → moléculas → organismos.
