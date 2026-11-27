/// Construye la pantalla o navegación de edit order screen conservando el flujo de usuario de Kotlin.
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';

import '../../data/local/orders/photo_compressor.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/molecules/rt_confirm_dialog.dart';
import '../../ui/designsystem/molecules/rt_error_state.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/molecules/rt_photo_picker.dart';
import '../../ui/designsystem/organisms/rt_action_bar.dart';
import '../../ui/designsystem/organisms/rt_item_list_editor.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/orders/edit_order_ui_state.dart';
import '../../viewmodel/orders/edit_order_view_model.dart';

/// Edita los materiales y fotos del pedido con diseño idéntico a Kotlin.
class EditOrderScreen extends ConsumerStatefulWidget {
  const EditOrderScreen({required this.orderId, super.key});
  final String orderId;

  @override
  ConsumerState<EditOrderScreen> createState() => _EditOrderScreenState();
}

class _EditOrderScreenState extends ConsumerState<EditOrderScreen> {
  final PhotoCompressor _photoCompressor = PhotoCompressor();

  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(editOrderViewModelProvider.notifier).onStart(widget.orderId));
  }

  Future<void> _choosePhoto() async {
    final viewModel = ref.read(editOrderViewModelProvider.notifier);
    final state = ref.read(editOrderViewModelProvider);
    if (state.photoCount >= 3 || state.saving) return;

    final source = await showModalBottomSheet<ImageSource>(
      context: context,
      builder: (context) => SafeArea(
        child: Wrap(
          children: [
            ListTile(
              leading: const Icon(Icons.photo_camera_outlined),
              title: const Text('Tomar foto'),
              onTap: () => Navigator.pop(context, ImageSource.camera),
            ),
            ListTile(
              leading: const Icon(Icons.photo_library_outlined),
              title: const Text('Elegir de la galería'),
              onTap: () => Navigator.pop(context, ImageSource.gallery),
            ),
          ],
        ),
      ),
    );
    if (source == null) return;
    final photo = await _photoCompressor.pick(source: source);
    if (photo != null && mounted) {
      viewModel.onPhotoAdded(photo.path);
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(editOrderViewModelProvider);
    final viewModel = ref.read(editOrderViewModelProvider.notifier);

    ref.listen(editOrderViewModelProvider.select((s) => s.finished), (previous, finished) {
      if (finished == true && mounted) {
        context.pop(true);
      }
    });

    final allPhotos = [
      ...state.keptPhotos.map((p) => p.url),
      ...state.newPhotos,
    ];

    return PopScope(
      canPop: !state.dirty && !state.saving,
      onPopInvokedWithResult: (didPop, result) {
        if (!didPop) viewModel.onBackRequested();
      },
      child: Scaffold(
        backgroundColor: Theme.of(context).scaffoldBackgroundColor,
        appBar: PreferredSize(
          preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
          child: SafeArea(
            bottom: false,
            child: RtTopAppBar(
              title: 'Editar pedido',
              onBack: () {
                if (viewModel.onBackRequested()) {
                  // ViewModel abrirá el diálogo
                } else {
                  context.pop(false);
                }
              },
            ),
          ),
        ),
        bottomNavigationBar: state.loading || state.finished || state.loadError != null
            ? null
            : RtActionBar(
                primary: RtButton(
                  key: const Key('btn_guardar_cambios'),
                  text: state.uploadingPhotos
                      ? 'Subiendo fotos…'
                      : state.saving
                          ? 'Guardando…'
                          : 'Guardar cambios',
                  onClick: viewModel.onSave,
                  loading: state.saving || state.uploadingPhotos,
                  fullWidth: true,
                ),
              ),
        body: Stack(
          children: [
            state.loading
                ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
                : state.loadError != null
                    ? Center(
                        child: RtErrorState(
                          title: 'No pudimos cargar el pedido',
                          message: state.loadError!,
                          onRetry: viewModel.onRetry,
                        ),
                      )
                    : state.finished
                        ? Center(
                            child: Padding(
                              padding: const EdgeInsets.all(RtSpacing.s4),
                              child: Column(
                                mainAxisSize: MainAxisSize.min,
                                children: [
                                  Text(
                                    state.finishMessage ?? 'Este pedido ya no se puede editar',
                                    style: Theme.of(context).textTheme.bodyLarge,
                                    textAlign: TextAlign.center,
                                  ),
                                  const SizedBox(height: RtSpacing.s4),
                                  RtButton(
                                    text: 'Volver',
                                    onClick: () => context.pop(false),
                                  ),
                                ],
                              ),
                            ),
                          )
                        : ListView(
                            padding: const EdgeInsets.all(RtSpacing.s4),
                            children: [
                              if (state.saveError != null) ...[
                                RtInlineAlert(
                                  tone: RtInlineAlertTone.error,
                                  message: state.saveError!,
                                ),
                                const SizedBox(height: RtSpacing.s4),
                              ],
                              RtItemListEditor(
                                items: state.items.map((item) => RtItemListEntry(
                                  description: item.description,
                                  weight: item.weight,
                                  error: item.error,
                                  descriptionInvalid: item.descriptionInvalid,
                                  weightInvalid: item.weightInvalid,
                                )).toList(),
                                totalWeight: state.totalWeightLabel,
                                onDescriptionChange: (index, value) => viewModel.onDescriptionChange(state.items[index].id, value),
                                onWeightChange: (index, value) => viewModel.onWeightChange(state.items[index].id, value),
                                onRemove: (index) => viewModel.onRemoveItem(state.items[index].id),
                                onAdd: viewModel.onAddItem,
                                error: state.itemsError,
                              ),
                              const SizedBox(height: RtSpacing.s4),
                              RtPhotoPicker(
                                photos: allPhotos,
                                onAdd: _choosePhoto,
                                onRemove: viewModel.onPhotoRemoved,
                                enabled: !state.saving,
                              ),
                              const SizedBox(height: RtSpacing.s4),
                              Row(
                                children: [
                                  Icon(
                                    Icons.place_rounded,
                                    size: RtSize.iconXs,
                                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                                  ),
                                  const SizedBox(width: RtSpacing.s1),
                                  Expanded(
                                    child: Text(
                                      'La ubicación no se edita. Si cambió, cancela este pedido y publica uno nuevo.',
                                      style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                            ],
                          ),
            if (state.discardDialogVisible)
              Positioned.fill(
                child: Material(
                  color: Colors.black54,
                  child: Center(
                    child: Padding(
                      padding: const EdgeInsets.all(RtSpacing.s4),
                      child: RtConfirmDialog(
                        key: const Key('dialog_descartar_cambios'),
                        title: '¿Descartar los cambios?',
                        message: 'Saldrás sin guardar lo que cambiaste en este pedido.',
                        confirmLabel: 'Descartar',
                        onConfirm: () {
                          viewModel.onDiscardDismissed();
                          context.pop(false);
                        },
                        onDismiss: viewModel.onDiscardDismissed,
                        dismissLabel: 'Seguir editando',
                        destructive: true,
                      ),
                    ),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }
}
