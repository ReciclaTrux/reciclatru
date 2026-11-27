/// Construye la pantalla o navegación de available order detail screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../model/orders/order_acceptance.dart';
import '../../ui/designsystem/atoms/rt_acceptance_chip.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/molecules/rt_confirm_dialog.dart';
import '../../ui/designsystem/molecules/rt_contact_row.dart';
import '../../ui/designsystem/molecules/rt_error_state.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/molecules/rt_meta_row.dart';
import '../../ui/designsystem/molecules/rt_snackbar.dart';
import '../../ui/designsystem/organisms/rt_action_bar.dart';
import '../../ui/designsystem/organisms/rt_photo_gallery.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/explore/available_order_detail_ui_state.dart';
import '../../viewmodel/explore/available_order_detail_view_model.dart';
import '../orders/materials_card.dart';
import '../orders/order_format.dart';
import 'available_order_format.dart';
import '../../model/explore/available_order_detail.dart';

/// Detalle del pedido para el reciclador antes y después de aceptarlo.
class AvailableOrderDetailScreen extends ConsumerStatefulWidget {
  const AvailableOrderDetailScreen({
    required this.orderId,
    this.onUnavailable,
    this.onCompleteProfile,
    super.key,
  });

  final String orderId;
  final VoidCallback? onUnavailable;
  final VoidCallback? onCompleteProfile;

  @override
  ConsumerState<AvailableOrderDetailScreen> createState() => _AvailableOrderDetailScreenState();
}

class _AvailableOrderDetailScreenState extends ConsumerState<AvailableOrderDetailScreen> {
  @override
  void initState() {
    super.initState();
    Future<void>.microtask(() => ref
        .read(availableOrderDetailViewModelProvider.notifier)
        .onStart(widget.orderId));
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(availableOrderDetailViewModelProvider);
    final viewModel = ref.read(availableOrderDetailViewModelProvider.notifier);
    ref.listen(availableOrderDetailViewModelProvider, (previous, next) {
      if (previous?.unavailable != true && next.unavailable) {
        WidgetsBinding.instance.addPostFrameCallback((_) => widget.onUnavailable?.call());
      }
      if (next.message != null && previous?.message != next.message) {
        showRtSnackbar(context, message: next.message!);
        viewModel.onMessageShown();
      }
    });

    return Stack(
      children: [
        _content(state, viewModel),
        if (state.confirmDialogVisible)
          _dialogBarrier(
            RtConfirmDialog(
              key: const Key('dialog_aceptar_pedido'),
              title: '¿Aceptar este pedido?',
              message: 'Al aceptar, el vecino verá tu nombre y celular. Si te elige, verás su dirección para ir a recoger.',
              confirmLabel: 'Aceptar',
              onConfirm: viewModel.onAcceptConfirmed,
              onDismiss: viewModel.onAcceptDismissed,
              loading: state.accepting,
              dismissLabel: 'Ahora no',
            ),
          ),
        if (state.profileIncomplete)
          _dialogBarrier(
            RtConfirmDialog(
              key: const Key('dialog_perfil_incompleto'),
              title: 'Completa tu perfil para aceptar',
              message: 'El vecino necesita tu nombre y celular para coordinar el recojo.',
              confirmLabel: 'Completar perfil',
              dismissLabel: 'Ahora no',
              onConfirm: () {
                viewModel.onProfileDialogDismissed();
                widget.onCompleteProfile?.call();
              },
              onDismiss: viewModel.onProfileDialogDismissed,
            ),
          ),
      ],
    );
  }

  /// Compone el Scaffold del detalle según el estado que expone el ViewModel.
  Widget _content(AvailableOrderDetailUiState state, AvailableOrderDetailViewModel viewModel) {
    final order = state.order;
    return Scaffold(
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(title: 'Detalle del pedido', onBack: () => context.pop()),
        ),
      ),
      bottomNavigationBar: !state.loading && order != null
          ? RtActionBar(
              primary: order.alreadyAccepted
                  ? RtButton(
                      key: const Key('btn_ya_aceptaste'),
                      text: 'Ya aceptaste este pedido',
                      onClick: () {},
                      variant: RtButtonVariant.secondary,
                      icon: Icons.check_rounded,
                      enabled: false,
                      fullWidth: true,
                    )
                  : RtButton(
                      key: const Key('btn_aceptar_pedido'),
                      text: 'Aceptar pedido',
                      onClick: viewModel.onAcceptClick,
                      fullWidth: true,
                    ),
            )
          : null,
      body: state.loading
          ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
          : state.loadError != null || order == null
              ? Center(
                  child: RtErrorState(
                    title: 'No pudimos cargar el pedido',
                    message: state.loadError ?? 'No pudimos completar la operación. Inténtalo de nuevo.',
                    onRetry: viewModel.onRetry,
                  ),
                )
              : ListView(
                  padding: const EdgeInsets.all(RtSpacing.s4),
                  children: order.alreadyAccepted ? _acceptedBody(order) : _availableBody(order),
                ),
    );
  }

  /// Muestra información permitida antes de que el reciclador acepte.
  List<Widget> _availableBody(AvailableOrderDetail order) => [
        if (order.photos.isNotEmpty) RtPhotoGallery(photos: order.photos.map((photo) => photo.url).toList()),
        Text(order.zone.isEmpty ? 'Trujillo' : order.zone, style: Theme.of(context).textTheme.titleLarge),
        RtMetaRow(items: _orderMeta(order)),
        MaterialsCard(items: order.items, totalWeightKg: order.totalWeightKg),
        const RtInlineAlert(
          tone: RtInlineAlertTone.info,
          message: 'Verás el nombre y celular del vecino al aceptar. La dirección exacta solo se muestra si te elige.',
        ),
      ];

  /// Muestra contacto solo si la API ya incluyó al vecino en la respuesta.
  List<Widget> _acceptedBody(AvailableOrderDetail order) {
    final neighbor = order.neighbor;
    return [
      Row(
        children: [
          Expanded(child: Text(order.zone.isEmpty ? 'Trujillo' : order.zone, style: Theme.of(context).textTheme.titleLarge)),
          RtAcceptanceChip(
            state: order.acceptanceStatus == AcceptanceStatus.chosen
                ? RtAcceptanceState.chosen
                : RtAcceptanceState.waiting,
          ),
        ],
      ),
      const RtInlineAlert(
        tone: RtInlineAlertTone.success,
        title: 'Aceptaste el pedido',
        message: 'El vecino elegirá a un reciclador. Desliza hacia abajo en Aceptados para saber si te eligió.',
      ),
      if (neighbor != null)
        _contactCard(
          name: neighbor.fullName,
          phone: neighbor.phone,
        ),
      _summaryCard(order),
    ];
  }

  /// Tarjeta de contacto protegida por los datos entregados por el backend.
  Widget _contactCard({required String name, required String phone}) => Container(
        padding: const EdgeInsets.all(RtSpacing.s4),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          border: Border.all(color: Theme.of(context).colorScheme.outlineVariant),
          borderRadius: BorderRadius.circular(RtRadii.lg),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Vecino', style: Theme.of(context).textTheme.labelLarge),
            const SizedBox(height: RtSpacing.s3),
            RtContactRow(
              name: name,
              subtitle: phone,
              phone: phone,
              onCall: () => launchUrl(Uri(scheme: 'tel', path: phone)),
            ),
            const SizedBox(height: RtSpacing.s2),
            const RtMetaRow(items: [RtMeta(Icons.lock_rounded, 'La dirección se muestra solo si te elige.')]),
          ],
        ),
      );

  /// Resumen compacto de materiales, peso, distancia y fecha de publicación.
  Widget _summaryCard(AvailableOrderDetail order) => Container(
        padding: const EdgeInsets.all(RtSpacing.s4),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          border: Border.all(color: Theme.of(context).colorScheme.outlineVariant),
          borderRadius: BorderRadius.circular(RtRadii.lg),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Materiales', style: Theme.of(context).textTheme.labelLarge),
            const SizedBox(height: RtSpacing.s3),
            Row(children: [
              Expanded(child: Text(formatItemCount(order.items.length))),
              Text(formatWeightKg(order.totalWeightKg)),
            ]),
            const SizedBox(height: RtSpacing.s3),
            RtMetaRow(items: [
              if (order.distanceKm != null) RtMeta(Icons.near_me_rounded, formatDistance(order.distanceKm)!),
              RtMeta(Icons.calendar_today_rounded, formatPublishedDate(order.createdAt)),
            ]),
          ],
        ),
      );

  /// Datos no sensibles que acompañan al resumen del pedido.
  List<RtMeta> _orderMeta(AvailableOrderDetail order) => [
        if (order.distanceKm != null) RtMeta(Icons.near_me_rounded, formatDistance(order.distanceKm)!),
        RtMeta(Icons.calendar_today_rounded, formatOrderDate(order.createdAt)),
        RtMeta(Icons.groups_rounded, _formatAcceptanceCount(order.acceptanceCount)),
      ];

  String _formatAcceptanceCount(int count) => switch (count) {
        0 => 'Sin aceptaciones',
        1 => '1 reciclador aceptó',
        _ => '$count recicladores aceptaron',
      };

  /// Presenta diálogo modal sobre la pantalla sin sacar su estado de ViewModel.
  Widget _dialogBarrier(Widget dialog) => Positioned.fill(
        child: Material(
          color: Colors.black54,
          child: Center(
            child: Padding(
              padding: const EdgeInsets.all(RtSpacing.s4),
              child: dialog,
            ),
          ),
        ),
      );
}
