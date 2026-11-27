/// Construye la pantalla o navegación de order detail screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../model/orders/order.dart';
import '../../model/orders/order_acceptance.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/atoms/rt_status_chip.dart';
import '../../ui/designsystem/molecules/rt_confirm_dialog.dart';
import '../../ui/designsystem/molecules/rt_contact_row.dart';
import '../../ui/designsystem/molecules/rt_error_state.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/organisms/rt_action_bar.dart';
import '../../ui/designsystem/organisms/rt_acceptance_card.dart';
import '../../ui/designsystem/organisms/rt_photo_gallery.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/orders/order_detail_view_model.dart';
import 'materials_card.dart';
import 'order_format.dart';

/// Detalle de pedido propio con diseño idéntico a Kotlin.
class OrderDetailScreen extends ConsumerStatefulWidget {
  const OrderDetailScreen({required this.orderId, super.key});
  final String orderId;

  @override
  ConsumerState<OrderDetailScreen> createState() => _OrderDetailScreenState();
}

class _OrderDetailScreenState extends ConsumerState<OrderDetailScreen> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(orderDetailViewModelProvider.notifier).onStart(widget.orderId));
  }

  Future<void> _chooseRecycler(OrderAcceptance acceptance) async {
    final viewModel = ref.read(orderDetailViewModelProvider.notifier);
    viewModel.onChooseClick(acceptance);
  }

  Future<void> _cancelOrder() async {
    final viewModel = ref.read(orderDetailViewModelProvider.notifier);
    viewModel.onCancelClick();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(orderDetailViewModelProvider);
    final viewModel = ref.read(orderDetailViewModelProvider.notifier);

    ref.listen(orderDetailViewModelProvider.select((value) => value.message), (previous, message) {
      if (message == null || message == previous) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      viewModel.onMessageShown();
    });

    final order = state.order;

    return Stack(
      children: [
        Scaffold(
          backgroundColor: Theme.of(context).scaffoldBackgroundColor,
          appBar: PreferredSize(
            preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
            child: SafeArea(
              bottom: false,
              child: RtTopAppBar(title: 'Detalle del pedido', onBack: () => context.pop()),
            ),
          ),
          bottomNavigationBar: order == null || state.loading || (!order.canEdit && !order.canCancel)
              ? null
              : RtActionBar(
                  primary: order.canEdit
                      ? RtButton(
                          text: 'Editar',
                          onClick: () async {
                            final result = await context.push<bool>('/orders/${order.id}/edit');
                            if (result == true && mounted) await viewModel.onRefresh();
                          },
                          icon: Icons.edit_rounded,
                          fullWidth: true,
                        )
                      : RtButton(
                          text: state.cancelling ? 'Cancelando…' : 'Cancelar pedido',
                          onClick: state.cancelling ? () {} : _cancelOrder,
                          variant: RtButtonVariant.secondary,
                          fullWidth: true,
                          enabled: !state.cancelling,
                        ),
                  secondary: order.canEdit && order.canCancel
                      ? RtButton(
                          text: state.cancelling ? 'Cancelando…' : 'Cancelar pedido',
                          onClick: state.cancelling ? () {} : _cancelOrder,
                          variant: RtButtonVariant.secondary,
                          fullWidth: true,
                          enabled: !state.cancelling,
                        )
                      : null,
                ),
          body: state.loading && order == null
              ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
              : state.loadError != null && order == null
                  ? Center(
                      child: RtErrorState(
                        title: 'No pudimos cargar el detalle',
                        message: state.loadError!,
                        onRetry: viewModel.onRetry,
                      ),
                    )
                  : order == null
                      ? const Center(child: Text('No encontramos el pedido.'))
                      : RefreshIndicator(
                          onRefresh: viewModel.onRefresh,
                          child: ListView(
                            padding: const EdgeInsets.all(RtSpacing.s4),
                            children: [
                              if (state.refreshing) const LinearProgressIndicator(),
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      formatOrderDate(order.createdAt),
                                      style: Theme.of(context).textTheme.titleMedium,
                                    ),
                                  ),
                                  RtStatusChip(status: _mapStatus(order.status)),
                                ],
                              ),
                              const SizedBox(height: RtSpacing.s4),
                              if (order.status == OrderStatus.pending)
                                const RtInlineAlert(
                                  tone: RtInlineAlertTone.info,
                                  title: 'Aún ningún reciclador aceptó tu pedido',
                                  message: 'Desliza hacia abajo para ver si alguien aceptó.',
                                ),
                              const SizedBox(height: RtSpacing.s4),
                              MaterialsCard(items: order.items, totalWeightKg: order.totalWeightKg),
                              if (order.photos.isNotEmpty) ...[
                                const SizedBox(height: RtSpacing.s4),
                                RtPhotoGallery(photos: order.photos.map((p) => p.url).toList()),
                              ],
                              if (order.canChooseRecycler) ...[
                                const SizedBox(height: RtSpacing.s5),
                                Text('Recicladores interesados', style: Theme.of(context).textTheme.titleMedium),
                                const SizedBox(height: RtSpacing.s3),
                                if (state.acceptancesLoading) const LinearProgressIndicator(),
                                if (state.acceptancesError != null)
                                  RtErrorState(
                                    title: 'No pudimos cargar los recicladores',
                                    message: state.acceptancesError!,
                                    onRetry: viewModel.onRetryAcceptances,
                                  ),
                                for (final acceptance in state.acceptances)
                                  Padding(
                                    padding: const EdgeInsets.only(bottom: RtSpacing.s3),
                                    child: RtAcceptanceCard(
                                      name: acceptance.fullName,
                                      phone: acceptance.phone,
                                      acceptedAt: acceptance.acceptedAt != null ? formatOrderDate(acceptance.acceptedAt!) : '',
                                      onCall: () => launchUrl(Uri(scheme: 'tel', path: acceptance.phone)),
                                      onChoose: state.choosing ? null : () => _chooseRecycler(acceptance),
                                      choosing: state.choosing,
                                    ),
                                  ),
                              ],
                              if (order.counterpart != null) ...[
                                const SizedBox(height: RtSpacing.s5),
                                Text('Reciclador elegido', style: Theme.of(context).textTheme.titleMedium),
                                const SizedBox(height: RtSpacing.s3),
                                RtContactRow(
                                  name: order.counterpart!.fullName,
                                  subtitle: order.counterpart!.phone,
                                  phone: order.counterpart!.phone,
                                  onCall: () => launchUrl(Uri(scheme: 'tel', path: order.counterpart!.phone)),
                                ),
                              ],
                            ],
                          ),
                        ),
        ),
        if (state.recyclerToChoose != null)
          _dialogBarrier(
            RtConfirmDialog(
              key: const Key('dialog_elegir_reciclador'),
              title: '¿Elegir a ${state.recyclerToChoose!.fullName}?',
              message: 'Él recogerá tu pedido. Los demás recicladores quedarán descartados y no podrás cambiar tu elección.',
              confirmLabel: 'Elegir',
              onConfirm: viewModel.onChooseConfirmed,
              onDismiss: viewModel.onChooseDismissed,
              loading: state.choosing,
            ),
          ),
        if (state.cancelDialogVisible && order != null)
          _dialogBarrier(
            RtConfirmDialog(
              key: const Key('dialog_cancelar_pedido'),
              title: '¿Seguro que deseas cancelar?',
              message: 'El pedido pasará a tu historial y no se podrá reactivar.',
              confirmLabel: 'Sí, cancelar',
              onConfirm: viewModel.onCancelConfirmed,
              onDismiss: viewModel.onCancelDismissed,
              destructive: true,
              loading: state.cancelling,
            ),
          ),
      ],
    );
  }

  RtOrderStatus _mapStatus(OrderStatus status) => switch (status) {
        OrderStatus.pending => RtOrderStatus.pending,
        OrderStatus.confirmed => RtOrderStatus.confirmed,
        OrderStatus.collected => RtOrderStatus.collected,
        OrderStatus.cancelled => RtOrderStatus.cancelled,
      };

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
