# Sistema de diseño — ReciclaTru Flutter

Implementación en Flutter del sistema de diseño de ReciclaTru, publicado en:
https://claude.ai/artifact/JZrHXR93AT3KsyobjzR6F2

El sistema **ya está definido**: tokens, 33 componentes `Rt*`, sus parámetros y sus
estados. Esta carpeta solo lo implementa; no se diseñan ni se inventan componentes acá.
Android y Flutter usan los mismos nombres y parámetros.

```text
lib/core/design_system/
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
   parámetros de "Qué recibe", y crear un ejemplo (Widgetbook) con los mismos estados que su
   vista previa. Además, todo componente nuevo agrega un caso **`Interactivo`** con knobs
   para editar sus parámetros desde el panel del catálogo.
6. **Stateless**: reciben datos y callbacks; nunca providers, repositorios ni modelos de dominio.
7. Los átomos envuelven Material 3, no lo reemplazan.
8. Si cambia un valor del sistema, se actualiza en las dos apps en el mismo PR.

## Plan por sprint

Antes de cada sprint se implementan los componentes que usan sus HU, más sus dependencias.

| Sprint | HU | Componentes |
| --- | --- | --- |
| 1 | HU-01 a HU-04 | átomos `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton`, `RtAvatar`, `RtBadge`; moléculas `RtRoleSelector`, `RtInlineAlert`, `RtSearchField`, `RtContactRow`, `RtConfirmDialog`, `RtSnackbar`, `RtErrorState`; organismos `RtTopAppBar`, `RtBottomNav`, `RtMapPicker`, `RtActionBar` |
| 2+ | por definir | los restantes, según el inventario por HU del sistema |

## Componentes por HU

Al desarrollar una HU, usa estos componentes; no crees otros. Si falta algo, propónlo
primero en el sistema de diseño.

Referencia visual: [prototipo de pantallas](https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh)
(38 tableros; cada HU se construye igual que su tablero, sin rediseñar).

| HU | Átomos | Moléculas | Organismos |
| --- | --- | --- | --- |
| HU-01 Registro | `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton` | `RtRoleSelector`, `RtInlineAlert` | – |
| HU-02 Login y sesión | `RtButton`, `RtTextField`, `RtSpinner`, `RtIconButton` | `RtInlineAlert` | – |
| HU-03 Completar perfil | `RtAvatar`, `RtBadge`, `RtButton`, `RtTextField`, `RtIconButton` | `RtContactRow`, `RtSnackbar`, `RtConfirmDialog`, `RtInlineAlert` | `RtTopAppBar`, `RtActionBar`, `RtBottomNav` |
| HU-04 Ubicación | `RtButton`, `RtTextField`, `RtIconButton` | `RtSearchField`, `RtErrorState`, `RtInlineAlert` | `RtMapPicker`, `RtTopAppBar`, `RtBottomNav` |
| HU-05 Publicar pedido | `RtButton`, `RtTextField`, `RtPhotoThumb`, `RtIconButton` | `RtItemRow`, `RtPhotoPicker`, `RtMetaRow`, `RtSnackbar` | `RtItemListEditor`, `RtTopAppBar`, `RtActionBar` |
| HU-06 Mis pedidos | `RtButton`, `RtStatusChip`, `RtBadge`, `RtSpinner` | `RtTabs`, `RtEmptyState`, `RtMetaRow`, `RtErrorState`, `RtSnackbar` | `RtOrderCard`, `RtTopAppBar`, `RtBottomNav`, `RtActionBar` |
| HU-07 Editar pedido | `RtButton`, `RtTextField`, `RtPhotoThumb`, `RtIconButton` | `RtItemRow`, `RtPhotoPicker`, `RtConfirmDialog`, `RtMetaRow` | `RtItemListEditor`, `RtTopAppBar`, `RtActionBar` |
| HU-08 Cancelar pedido | `RtButton`, `RtStatusChip`, `RtAvatar`, `RtIconButton` | `RtConfirmDialog`, `RtInlineAlert`, `RtContactRow` | `RtTopAppBar`, `RtActionBar` |
| HU-09 Subir fotos | `RtPhotoThumb`, `RtSpinner` | `RtPhotoPicker`, `RtSnackbar` | – |
| HU-10 Pedidos disponibles | `RtBadge`, `RtIconButton` | `RtMetaRow`, `RtLocationPrompt`, `RtInlineAlert`, `RtEmptyState`, `RtErrorState` | `RtAvailableOrderCard`, `RtTopAppBar`, `RtBottomNav` |
| HU-11 Detalle del pedido | `RtButton`, `RtIconButton` | `RtMetaRow`, `RtInlineAlert` | `RtPhotoGallery`, `RtActionBar`, `RtTopAppBar` |
| HU-12 Aceptar pedido | `RtButton`, `RtIconButton` | `RtConfirmDialog`, `RtContactRow`, `RtMetaRow` | `RtTopAppBar`, `RtActionBar` |
| HU-13 Elegir reciclador | `RtAvatar`, `RtButton`, `RtIconButton` | `RtContactRow`, `RtConfirmDialog`, `RtEmptyState` | `RtAcceptanceCard`, `RtTopAppBar`, `RtActionBar` |
| HU-14 Mis aceptados | `RtAcceptanceChip`, `RtBadge`, `RtIconButton` | `RtTabs`, `RtEmptyState`, `RtMetaRow` | `RtAcceptedOrderCard`, `RtTopAppBar`, `RtBottomNav` |
| HU-15 Ir a recoger | `RtAvatar`, `RtButton`, `RtIconButton`, `RtAcceptanceChip` | `RtAddressBlock`, `RtContactRow`, `RtInlineAlert`, `RtSnackbar` | `RtTopAppBar`, `RtActionBar` |
| HU-16 Marcar Recolectado | `RtButton`, `RtStatusChip`, `RtAvatar`, `RtIconButton` | `RtConfirmDialog`, `RtInlineAlert`, `RtContactRow` | `RtActionBar`, `RtTopAppBar` |
| Todas | `RtSpinner` | `RtErrorState` | `RtTopAppBar`, `RtBottomNav` |

### Estado de implementación

| Componente | Nivel | Estado | Archivo |
| --- | --- | --- | --- |
| Tema `rtTheme` | tema | listo | `lib/core/design_system/theme/` |
| `RtAvatar` | átomo | listo | `lib/core/design_system/atoms/rt_avatar.dart` |
| `RtBadge` | átomo | listo | `lib/core/design_system/atoms/rt_badge.dart` |
| `RtButton` | átomo | listo | `lib/core/design_system/atoms/rt_button.dart` |
| `RtIconButton` | átomo | listo | `lib/core/design_system/atoms/rt_icon_button.dart` |
| `RtSpinner` | átomo | listo | `lib/core/design_system/atoms/rt_spinner.dart` |
| `RtTextField` | átomo | listo | `lib/core/design_system/atoms/rt_text_field.dart` |
| `RtAcceptanceChip` | átomo | pendiente | `lib/core/design_system/atoms/rt_acceptance_chip.dart` |
| `RtPhotoThumb` | átomo | pendiente | `lib/core/design_system/atoms/rt_photo_thumb.dart` |
| `RtStatusChip` | átomo | pendiente | `lib/core/design_system/atoms/rt_status_chip.dart` |
| `RtRoleSelector` | molécula | listo | `lib/core/design_system/molecules/rt_role_selector.dart` |
| `RtInlineAlert` | molécula | listo | `lib/core/design_system/molecules/rt_inline_alert.dart` |
| `RtSearchField` | molécula | listo | `lib/core/design_system/molecules/rt_search_field.dart` |
| `RtContactRow` | molécula | listo | `lib/core/design_system/molecules/rt_contact_row.dart` |
| `RtConfirmDialog` | molécula | listo | `lib/core/design_system/molecules/rt_confirm_dialog.dart` |
| `RtSnackbar` | molécula | listo | `lib/core/design_system/molecules/rt_snackbar.dart` |
| `RtErrorState` | molécula | listo | `lib/core/design_system/molecules/rt_error_state.dart` |
| `RtItemRow` | molécula | pendiente | `lib/core/design_system/molecules/rt_item_row.dart` |
| `RtPhotoPicker` | molécula | pendiente | `lib/core/design_system/molecules/rt_photo_picker.dart` |
| `RtTabs` | molécula | pendiente | `lib/core/design_system/molecules/rt_tabs.dart` |
| `RtEmptyState` | molécula | pendiente | `lib/core/design_system/molecules/rt_empty_state.dart` |
| `RtMetaRow` | molécula | pendiente | `lib/core/design_system/molecules/rt_meta_row.dart` |
| `RtAddressBlock` | molécula | pendiente | `lib/core/design_system/molecules/rt_address_block.dart` |
| `RtTopAppBar` | organismo | listo | `lib/core/design_system/organisms/rt_top_app_bar.dart` |
| `RtBottomNav` | organismo | listo | `lib/core/design_system/organisms/rt_bottom_nav.dart` |
| `RtMapPicker` | organismo | listo | `lib/core/design_system/organisms/rt_map_picker.dart` |
| `RtActionBar` | organismo | listo | `lib/core/design_system/organisms/rt_action_bar.dart` |
| `RtItemListEditor` | organismo | pendiente | `lib/core/design_system/organisms/rt_item_list_editor.dart` |
| `RtOrderCard` | organismo | pendiente | `lib/core/design_system/organisms/rt_order_card.dart` |
| `RtAvailableOrderCard` | organismo | pendiente | `lib/core/design_system/organisms/rt_available_order_card.dart` |
| `RtLocationPrompt` | organismo | pendiente | `lib/core/design_system/organisms/rt_location_prompt.dart` |
| `RtPhotoGallery` | organismo | pendiente | `lib/core/design_system/organisms/rt_photo_gallery.dart` |
| `RtAcceptanceCard` | organismo | pendiente | `lib/core/design_system/organisms/rt_acceptance_card.dart` |
| `RtAcceptedOrderCard` | organismo | pendiente | `lib/core/design_system/organisms/rt_accepted_order_card.dart` |

Prototipo de pantallas (referencia visual): https://claude.ai/artifact/ECANZst8MApzL3Yn6x1fVh
