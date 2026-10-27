# Sistema de diseño — ReciclaTru Compose

Implementación en Compose del sistema de diseño de ReciclaTru, publicado en:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

El sistema **ya está definido**: tokens, 33 componentes `Rt*`, sus parámetros y sus
estados. Esta carpeta solo lo implementa; no se diseñan ni se inventan componentes acá.
Android y Flutter usan los mismos nombres y parámetros.

```text
ui/designsystem/
├── theme/        # tokens → Material 3 + extensión propia
├── atoms/        # 9 componentes
├── molecules/    # 13 componentes
├── organisms/    # 11 componentes
└── README.md
```

No hay nivel de plantillas ni carpeta de pantallas: las pantallas viven en cada feature.

| Nivel | Cantidad | Componentes |
| --- | --- | --- |
| Átomos | 9 | `RtAcceptanceChip`, `RtAvatar`, `RtBadge`, `RtButton`, `RtIconButton`, `RtPhotoThumb`, `RtSpinner`, `RtStatusChip`, `RtTextField` |
| Moléculas | 13 | `RtAddressBlock`, `RtConfirmDialog`, `RtContactRow`, `RtEmptyState`, `RtErrorState`, `RtInlineAlert`, `RtItemRow`, `RtMetaRow`, `RtPhotoPicker`, `RtRoleSelector`, `RtSearchField`, `RtSnackbar`, `RtTabs` |
| Organismos | 11 | `RtAcceptanceCard`, `RtAcceptedOrderCard`, `RtActionBar`, `RtAvailableOrderCard`, `RtBottomNav`, `RtItemListEditor`, `RtLocationPrompt`, `RtMapPicker`, `RtOrderCard`, `RtPhotoGallery`, `RtTopAppBar` |

## Reglas

1. **Existe en el sistema → se usa; no existe → no se inventa.** Antes de crear algo,
   buscar su `Rt*` en el sistema. Prohibido crear componentes equivalentes, variantes
   locales o copias dentro de una feature.
2. **Falta algo para una HU**: se propone primero en el sistema de diseño y, aprobado, se
   implementa en las dos apps con el mismo nombre. No se resuelve en la pantalla.
3. **Orden de construcción**: tema (colores, tipografía, espaciado, formas) → átomos →
   moléculas → organismos. Un átomo solo usa tokens; una molécula, átomos; un organismo,
   moléculas y átomos.  Se implementan por sprint (ver abajo).
4. **Qué usa una pantalla**: si hay molécula u organismo para eso, se usa; no se rearma con
   átomos. Los átomos van directos solo cuando ningún componente mayor cubre el caso.
5. **Por cada componente**: leer su guía en el sistema, respetar el nombre `Rt…` y los
   parámetros de "Qué recibe", y crear `@Preview` con los mismos estados que su vista
   previa.
6. **Stateless**: reciben datos y callbacks; nunca ViewModels, repositorios ni modelos de dominio.
7. Los átomos envuelven Material 3, no lo reemplazan.
8. Si cambia un valor del sistema, se actualiza en las dos apps en el mismo PR.

## Plan por sprint

Antes de cada sprint se implementan los componentes que usan sus HU, más sus dependencias.

| Sprint | HU | Componentes |
| --- | --- | --- |
| 1 | HU-01 a HU-04 | átomos `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton`, `RtAvatar`, `RtBadge`; moléculas `RtRoleSelector`, `RtInlineAlert`, `RtSearchField`, `RtContactRow`, `RtConfirmDialog`, `RtSnackbar`, `RtErrorState`; organismos `RtTopAppBar`, `RtBottomNav`, `RtMapPicker`, `RtActionBar` |
| 2+ | por definir | los restantes, según el inventario por HU del sistema |

Prototipo de pantallas (referencia visual): https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh

## Componentes por HU

Al desarrollar una HU, usa estos componentes; no crees otros. Si falta algo, propónlo primero en el
sistema de diseño.

Referencia visual de cada pantalla: https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh
(cada tablero indica su HU en `canvas.json`).

La tabla lista **todo lo que usa cada HU**, tanto lo nuevo como lo reutilizado, según el
inventario por HU del sistema. El estado y el archivo de cada componente están en la
segunda tabla.

| HU | Átomos | Moléculas | Organismos |
| --- | --- | --- | --- |
| HU-01 Registro | `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton` | `RtRoleSelector`, `RtInlineAlert` | – |
| HU-02 Login y sesión | `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton` | `RtInlineAlert` | – |
| HU-03 Completar perfil | `RtAvatar`, `RtBadge`, `RtButton`, `RtTextField`, `RtSpinner` | `RtContactRow`, `RtSnackbar`, `RtConfirmDialog` | `RtTopAppBar`, `RtActionBar`, `RtBottomNav` |
| HU-04 Ubicación | `RtButton`, `RtTextField`, `RtSpinner` | `RtSearchField`, `RtErrorState`, `RtInlineAlert` | `RtMapPicker` |
| HU-05 Publicar pedido | `RtPhotoThumb`, `RtButton`, `RtTextField`, `RtSpinner` | `RtItemRow`, `RtPhotoPicker`, `RtSnackbar` | `RtItemListEditor`, `RtActionBar` |
| HU-06 Mis pedidos | `RtStatusChip`, `RtBadge`, `RtSpinner` | `RtTabs`, `RtEmptyState`, `RtMetaRow`, `RtErrorState` | `RtOrderCard`, `RtTopAppBar`, `RtBottomNav` |
| HU-07 Editar pedido | `RtPhotoThumb`, `RtButton`, `RtTextField`, `RtSpinner` | `RtItemRow`, `RtPhotoPicker`, `RtConfirmDialog`, `RtSnackbar` | `RtItemListEditor`, `RtActionBar` |
| HU-08 Cancelar pedido | `RtButton`, `RtSpinner` | `RtConfirmDialog`, `RtInlineAlert`, `RtSnackbar` | `RtActionBar` |
| HU-09 Subir fotos | `RtPhotoThumb`, `RtSpinner` | `RtPhotoPicker`, `RtSnackbar` | – |
| HU-10 Pedidos disponibles | `RtButton`, `RtSpinner` | `RtMetaRow`, `RtInlineAlert`, `RtEmptyState`, `RtErrorState` | `RtAvailableOrderCard`, `RtLocationPrompt`, `RtTopAppBar`, `RtBottomNav` |
| HU-11 Detalle del pedido | `RtButton`, `RtSpinner` | `RtMetaRow`, `RtErrorState` | `RtPhotoGallery`, `RtActionBar`, `RtTopAppBar` |
| HU-12 Aceptar pedido | `RtButton`, `RtSpinner` | `RtConfirmDialog`, `RtContactRow`, `RtSnackbar` | `RtActionBar` |
| HU-13 Elegir reciclador | `RtAvatar`, `RtButton`, `RtSpinner` | `RtConfirmDialog`, `RtContactRow`, `RtEmptyState`, `RtErrorState` | `RtAcceptanceCard`, `RtTopAppBar` |
| HU-14 Mis aceptados | `RtAcceptanceChip`, `RtBadge`, `RtSpinner` | `RtTabs`, `RtEmptyState`, `RtErrorState` | `RtAcceptedOrderCard`, `RtTopAppBar`, `RtBottomNav` |
| HU-15 Ir a recoger | `RtButton`, `RtSpinner` | `RtAddressBlock`, `RtContactRow`, `RtInlineAlert`, `RtSnackbar` | `RtActionBar`, `RtTopAppBar` |
| HU-16 Marcar Recolectado | `RtButton`, `RtSpinner` | `RtConfirmDialog`, `RtSnackbar` | `RtActionBar` |
| Todas | `RtSpinner` | `RtErrorState` | `RtTopAppBar`, `RtBottomNav` |

### Estado y archivo por componente

| Nivel | Componente | Estado | Archivo |
| --- | --- | --- | --- |
| tema | `RtTheme` | listo | `ui/designsystem/theme/RtTheme.kt` |
| átomo | `RtAvatar` | listo | `ui/designsystem/atoms/RtAvatar.kt` |
| átomo | `RtBadge` | listo | `ui/designsystem/atoms/RtBadge.kt` |
| átomo | `RtButton` | listo | `ui/designsystem/atoms/RtButton.kt` |
| átomo | `RtIconButton` | listo | `ui/designsystem/atoms/RtIconButton.kt` |
| átomo | `RtSpinner` | listo | `ui/designsystem/atoms/RtSpinner.kt` |
| átomo | `RtTextField` | listo | `ui/designsystem/atoms/RtTextField.kt` |
| átomo | `RtAcceptanceChip` | pendiente | `ui/designsystem/atoms/RtAcceptanceChip.kt` |
| átomo | `RtPhotoThumb` | pendiente | `ui/designsystem/atoms/RtPhotoThumb.kt` |
| átomo | `RtStatusChip` | pendiente | `ui/designsystem/atoms/RtStatusChip.kt` |
| molécula | `RtAddressBlock` | pendiente | `ui/designsystem/molecules/RtAddressBlock.kt` |
| molécula | `RtConfirmDialog` | pendiente | `ui/designsystem/molecules/RtConfirmDialog.kt` |
| molécula | `RtContactRow` | pendiente | `ui/designsystem/molecules/RtContactRow.kt` |
| molécula | `RtEmptyState` | pendiente | `ui/designsystem/molecules/RtEmptyState.kt` |
| molécula | `RtErrorState` | pendiente | `ui/designsystem/molecules/RtErrorState.kt` |
| molécula | `RtInlineAlert` | pendiente | `ui/designsystem/molecules/RtInlineAlert.kt` |
| molécula | `RtItemRow` | pendiente | `ui/designsystem/molecules/RtItemRow.kt` |
| molécula | `RtMetaRow` | pendiente | `ui/designsystem/molecules/RtMetaRow.kt` |
| molécula | `RtPhotoPicker` | pendiente | `ui/designsystem/molecules/RtPhotoPicker.kt` |
| molécula | `RtRoleSelector` | pendiente | `ui/designsystem/molecules/RtRoleSelector.kt` |
| molécula | `RtSearchField` | pendiente | `ui/designsystem/molecules/RtSearchField.kt` |
| molécula | `RtSnackbar` | pendiente | `ui/designsystem/molecules/RtSnackbar.kt` |
| molécula | `RtTabs` | pendiente | `ui/designsystem/molecules/RtTabs.kt` |
| organismo | `RtAcceptanceCard` | pendiente | `ui/designsystem/organisms/RtAcceptanceCard.kt` |
| organismo | `RtAcceptedOrderCard` | pendiente | `ui/designsystem/organisms/RtAcceptedOrderCard.kt` |
| organismo | `RtActionBar` | pendiente | `ui/designsystem/organisms/RtActionBar.kt` |
| organismo | `RtAvailableOrderCard` | pendiente | `ui/designsystem/organisms/RtAvailableOrderCard.kt` |
| organismo | `RtBottomNav` | pendiente | `ui/designsystem/organisms/RtBottomNav.kt` |
| organismo | `RtItemListEditor` | pendiente | `ui/designsystem/organisms/RtItemListEditor.kt` |
| organismo | `RtLocationPrompt` | pendiente | `ui/designsystem/organisms/RtLocationPrompt.kt` |
| organismo | `RtMapPicker` | pendiente | `ui/designsystem/organisms/RtMapPicker.kt` |
| organismo | `RtOrderCard` | pendiente | `ui/designsystem/organisms/RtOrderCard.kt` |
| organismo | `RtPhotoGallery` | pendiente | `ui/designsystem/organisms/RtPhotoGallery.kt` |
| organismo | `RtTopAppBar` | pendiente | `ui/designsystem/organisms/RtTopAppBar.kt` |

El catálogo de lo implementado se abre solo en builds de debug: actividad
`ui/catalog/RtCatalogActivity` (`app/src/debug/`), con el mismo contenido que las vistas
previas de cada componente.
