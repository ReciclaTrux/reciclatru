/// Construye la pantalla o navegación de my orders screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../model/orders/order.dart';
import '../../model/orders/order_summary.dart';
import '../../viewmodel/orders/my_orders_view_model.dart';
import '../../viewmodel/orders/my_orders_ui_state.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/organisms/rt_bottom_nav.dart';
import '../../ui/designsystem/organisms/rt_order_card.dart';
import '../../ui/designsystem/atoms/rt_status_chip.dart';
import '../../ui/designsystem/molecules/rt_tabs.dart';
import '../../ui/designsystem/molecules/rt_empty_state.dart';
import '../../ui/designsystem/molecules/rt_error_state.dart';
import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import 'order_format.dart';

/// Pantalla inicial del vecino con diseño idéntico a Kotlin.
class MyOrdersScreen extends ConsumerWidget {
  const MyOrdersScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final uiState = ref.watch(myOrdersProvider);
    final current = uiState.tab(uiState.selectedTab);
    final viewModel = ref.read(myOrdersProvider.notifier);

    ref.listen(myOrdersProvider.select((state) => state.message), (previous, message) {
      if (message == null || message == previous) return;
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(message)));
      viewModel.onMessageShown();
    });

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      bottomNavigationBar: RtBottomNav(
        selected: 'orders',
        items: const [
          RtBottomNavItem(icon: Icons.receipt_long_rounded, label: 'Mis pedidos', route: 'orders'),
          RtBottomNavItem(icon: Icons.add_circle_rounded, label: 'Publicar', route: 'publish'),
          RtBottomNavItem(icon: Icons.person_rounded, label: 'Perfil', route: 'profile'),
        ],
        onSelect: (route) async {
          if (route == 'publish') {
            await context.push('/orders/create');
            viewModel.onRefresh();
          }
          if (route == 'profile') context.push('/profile');
        },
      ),
      body: SafeArea(
        child: Column(
          children: [
            const RtTopAppBar(title: 'Mis pedidos'),
            RtTabs(
              tabs: [
                RtTab(label: 'Activos', count: uiState.active.loaded ? uiState.active.orders.length : null),
                RtTab(label: 'Historial', count: uiState.history.loaded ? uiState.history.orders.length : null),
              ],
              selectedIndex: uiState.selectedTab == OrdersTab.active ? 0 : 1,
              onSelect: (index) => viewModel.onTabSelected(
                index == 0 ? OrdersTab.active : OrdersTab.history,
              ),
            ),
            Expanded(
              child: current.loading && !current.loaded
                  ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
                  : current.error != null && !current.loaded
                      ? Center(
                          child: RtErrorState(
                            title: 'No pudimos cargar tus pedidos',
                            message: current.error!,
                            onRetry: viewModel.onRetry,
                          ),
                        )
                      : RefreshIndicator(
                          onRefresh: viewModel.onRefresh,
                          child: current.orders.isEmpty
                              ? ListView(
                                  children: [
                                    const SizedBox(height: 120),
                                    if (uiState.selectedTab == OrdersTab.active)
                                      RtEmptyState(
                                        icon: Icons.recycling_rounded,
                                        title: 'Aún no publicaste pedidos',
                                        message: 'Publica el material que tienes y un reciclador de tu zona lo recogerá.',
                                        actionLabel: 'Publicar pedido',
                                        actionIcon: Icons.add_rounded,
                                        onAction: () async {
                                          await context.push('/orders/create');
                                          viewModel.onRefresh();
                                        },
                                      )
                                    else
                                      const RtEmptyState(
                                        icon: Icons.history_rounded,
                                        title: 'Tu historial está vacío',
                                        message: 'Aquí verás tus pedidos recolectados y cancelados.',
                                      ),
                                  ],
                                )
                              : ListView.builder(
                                  padding: const EdgeInsets.all(RtSpacing.s4),
                                  itemCount: current.orders.length + (current.loadingMore ? 1 : 0),
                                  itemBuilder: (context, index) {
                                    if (index >= current.orders.length) {
                                      return const Center(
                                        child: Padding(
                                          padding: EdgeInsets.all(RtSpacing.s4),
                                          child: RtSpinner(),
                                        ),
                                      );
                                    }
                                    final order = current.orders[index];
                                    return Padding(
                                      padding: const EdgeInsets.only(bottom: RtSpacing.s3),
                                      child: RtOrderCard(
                                        title: formatOrderDate(order.createdAt),
                                        status: _mapStatus(order.status),
                                        items: formatItemCount(order.itemCount),
                                        weight: formatWeightKg(order.totalWeightKg),
                                        onClick: () async {
                                          await context.push('/orders/${order.id}');
                                          viewModel.onRefresh();
                                        },
                                        footer: _mapFooter(order),
                                        thumbnail: order.thumbnailUrl,
                                      ),
                                    );
                                  },
                                ),
                        ),
            ),
          ],
        ),
      ),
    );
  }

  RtOrderStatus _mapStatus(OrderStatus status) => switch (status) {
        OrderStatus.pending => RtOrderStatus.pending,
        OrderStatus.confirmed => RtOrderStatus.confirmed,
        OrderStatus.collected => RtOrderStatus.collected,
        OrderStatus.cancelled => RtOrderStatus.cancelled,
      };

  RtOrderCardFooter? _mapFooter(OrderSummary order) {
    if (order.status == OrderStatus.pending) {
      final count = order.acceptanceCount;
      if (count == null || count == 0) return const RtOrderCardFooter(icon: Icons.group_rounded, text: 'Sin aceptaciones aún');
      if (count == 1) return const RtOrderCardFooter(icon: Icons.group_rounded, text: '1 aceptación');
      return RtOrderCardFooter(icon: Icons.group_rounded, text: '$count aceptaciones');
    }
    if (order.status == OrderStatus.confirmed && order.chosenRecyclerName != null) {
      return RtOrderCardFooter(icon: Icons.person_rounded, text: order.chosenRecyclerName!);
    }
    return null;
  }
}
