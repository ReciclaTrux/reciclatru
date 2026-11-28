/// Construye la pantalla o navegación de create order screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:image_picker/image_picker.dart';
import '../../data/local/orders/photo_compressor.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/molecules/rt_confirm_dialog.dart';
import '../../ui/designsystem/molecules/rt_photo_picker.dart';
import '../../ui/designsystem/organisms/rt_action_bar.dart';
import '../../ui/designsystem/organisms/rt_item_list_editor.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/orders/create_order_view_model.dart';

/// Formulario para publicar pedido usando el sistema de diseño exacto de Kotlin.
class CreateOrderScreen extends ConsumerStatefulWidget {
  const CreateOrderScreen({super.key});

  @override
  ConsumerState<CreateOrderScreen> createState() => _CreateOrderScreenState();
}

class _CreateOrderScreenState extends ConsumerState<CreateOrderScreen> {
  final _photoCompressor = PhotoCompressor();

  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(createOrderViewModelProvider.notifier).onStart());
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(createOrderViewModelProvider);
    final viewModel = ref.read(createOrderViewModelProvider.notifier);

    ref.listen(createOrderViewModelProvider.select((s) => s.published), (previous, published) {
      if (published == true && mounted) {
        context.pop(state.publishedOrderId);
      }
    });

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(title: 'Publicar pedido', onBack: () => context.pop()),
        ),
      ),
      bottomNavigationBar: RtActionBar(
        primary: RtButton(
          key: const Key('btn_publicar_pedido'),
          text: state.uploadingPhotos
              ? 'Subiendo fotos…'
              : state.publishing
                  ? 'Publicando…'
                  : 'Publicar pedido',
          onClick: viewModel.onPublish,
          loading: state.publishing || state.uploadingPhotos,
          fullWidth: true,
        ),
      ),
      body: Stack(
        children: [
          ListView(
            padding: const EdgeInsets.all(RtSpacing.s4),
            children: [
              if (state.publishError != null) ...[
                RtInlineAlert(
                  key: const Key('alert_publicar_error'),
                  tone: RtInlineAlertTone.error,
                  message: state.publishError!,
                ),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtItemListEditor(
                key: const Key('editor_items_pedido'),
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
                key: const Key('selector_fotos_pedido'),
                photos: state.photos,
                onAdd: () => _choosePhoto(viewModel),
                onRemove: viewModel.onPhotoRemoved,
                enabled: !state.publishing,
              ),
              if (state.pickupAddress != null) ...[
                const SizedBox(height: RtSpacing.s4),
                Row(
                  key: const Key('text_punto_recojo'),
                  children: [
                    Icon(
                      Icons.place_rounded,
                      size: RtSize.iconXs,
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
                    const SizedBox(width: RtSpacing.s1),
                    Expanded(
                      child: Text(
                        'Se recogerá en ${state.pickupAddress}',
                        style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                          color: Theme.of(context).colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ],
          ),
          if (state.profileIncomplete)
            Positioned.fill(
              child: Material(
                color: Colors.black54,
                child: Center(
                  child: Padding(
                    padding: const EdgeInsets.all(RtSpacing.s4),
                    child: RtConfirmDialog(
                      key: const Key('dialog_perfil_incompleto'),
                      title: 'Completa tu perfil para publicar',
                      message: 'Falta marcar tu punto de recojo. Sin él, el reciclador no sabrá a dónde ir.',
                      confirmLabel: 'Marcar ubicación',
                      onConfirm: () {
                        viewModel.onProfileDialogDismissed();
                        context.push('/pickup-location');
                      },
                      onDismiss: () {
                        viewModel.onProfileDialogDismissed();
                        context.pop();
                      },
                      dismissLabel: 'Ahora no',
                    ),
                    ),
                ),
              ),
            ),
        ],
      ),
    );
  }

  Future<void> _choosePhoto(CreateOrderViewModel viewModel) async {
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
}
