/// Construye la pantalla o navegación de available orders screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:geolocator/geolocator.dart';
import 'package:go_router/go_router.dart';

import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/molecules/rt_empty_state.dart';
import '../../ui/designsystem/molecules/rt_error_state.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/molecules/rt_snackbar.dart';
import '../../ui/designsystem/organisms/rt_available_order_card.dart';
import '../../ui/designsystem/organisms/rt_bottom_nav.dart';
import '../../ui/designsystem/organisms/rt_location_prompt.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/explore/available_orders_ui_state.dart';
import '../../viewmodel/explore/available_orders_view_model.dart';
import 'available_order_format.dart';

/// Inicio del reciclador: muestra y actualiza los pedidos que puede aceptar.
class AvailableOrdersScreen extends ConsumerStatefulWidget {
  const AvailableOrdersScreen({super.key});

  @override
  ConsumerState<AvailableOrdersScreen> createState() => _AvailableOrdersScreenState();
}

class _AvailableOrdersScreenState extends ConsumerState<AvailableOrdersScreen>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    Future<void>.microtask(_start);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  /// Inicializa el listado sin pedir permiso hasta que se acepte el aviso.
  Future<void> _start() async {
    final permission = await Geolocator.checkPermission();
    await ref.read(availableOrdersViewModelProvider.notifier).onStart(_granted(permission));
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) _refreshPermission();
  }

  /// Revisa si la persona cambió el permiso desde los ajustes del dispositivo.
  Future<void> _refreshPermission() async {
    final permission = await Geolocator.checkPermission();
    await ref.read(availableOrdersViewModelProvider.notifier).onResume(_granted(permission));
  }

  /// Solicita el permiso del sistema después de que la persona elige Continuar.
  Future<void> _requestPermission() async {
    await ref.read(availableOrdersViewModelProvider.notifier).onPromptAccepted();
    final permission = await Geolocator.requestPermission();
    final permanent = permission == LocationPermission.deniedForever;
    await ref.read(availableOrdersViewModelProvider.notifier)
        .onPermissionResult(_granted(permission), permanent);
  }

  /// Ejecuta la acción apropiada para el estado del permiso o del servicio.
  Future<void> _openLocationSetting(LocationNotice notice) async {
    switch (notice) {
      case LocationNotice.permissionDenied:
        await _requestPermission();
        break;
      case LocationNotice.permissionDeniedPermanently:
        await Geolocator.openAppSettings();
        break;
      case LocationNotice.unavailable:
        await Geolocator.openLocationSettings();
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(availableOrdersViewModelProvider);
    final viewModel = ref.read(availableOrdersViewModelProvider.notifier);

    ref.listen(availableOrdersViewModelProvider, (previous, next) {
      if (next.message != null && previous?.message != next.message) {
        showRtSnackbar(context, message: next.message!);
        viewModel.onMessageShown();
      }
    });

    return Scaffold(
      appBar: const PreferredSize(
        preferredSize: Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(bottom: false, child: RtTopAppBar(title: 'Pedidos disponibles')),
      ),
      bottomNavigationBar: RtBottomNav(
        items: const [
          RtBottomNavItem(icon: Icons.travel_explore_rounded, label: 'Disponibles', route: 'available'),
          RtBottomNavItem(icon: Icons.assignment_turned_in_rounded, label: 'Aceptados', route: 'accepted'),
          RtBottomNavItem(icon: Icons.person_rounded, label: 'Perfil', route: 'profile'),
        ],
        selected: 'available',
        onSelect: (route) {
          if (route == 'profile') context.push('/profile');
        },
      ),
      body: _body(state, viewModel),
    );
  }

  /// Renderiza el aviso, carga inicial, error, vacío o lista cargada.
  Widget _body(AvailableOrdersUiState state, AvailableOrdersViewModel viewModel) {
    if (state.showLocationPrompt) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(RtSpacing.s4),
          child: RtLocationPrompt(
            onContinue: _requestPermission,
            onDismiss: viewModel.onPromptDismissed,
          ),
        ),
      );
    }
    if (state.loading || (!state.loaded && state.loadError == null)) {
      return const Center(child: RtSpinner(size: RtSpinnerSize.large));
    }
    if (state.loadError != null) {
      return Center(
        child: RtErrorState(
          title: 'No pudimos cargar los pedidos',
          message: state.loadError!,
          onRetry: viewModel.onRetry,
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: viewModel.onRefresh,
      child: NotificationListener<ScrollNotification>(
        onNotification: (notification) {
          if (notification.metrics.pixels >= notification.metrics.maxScrollExtent - 200 &&
              state.nextCursor != null && !state.loadingMore) {
            viewModel.onLoadMore();
          }
          return false;
        },
        child: ListView(
          key: const Key('lista_pedidos_disponibles'),
          padding: const EdgeInsets.all(RtSpacing.s4),
          children: [
            if (state.locationNotice != null) ...[
              RtInlineAlert(
                tone: RtInlineAlertTone.info,
                title: 'Activa tu ubicación para ver la distancia',
                message: 'Mientras tanto, los pedidos se ordenan del más reciente al más antiguo.',
                actionLabel: state.locationNotice == LocationNotice.permissionDenied
                    ? 'Activar ubicación'
                    : 'Abrir ajustes',
                onAction: () => _openLocationSetting(state.locationNotice!),
              ),
              const SizedBox(height: RtSpacing.s3),
            ],
            if (state.orders.isEmpty)
              const RtEmptyState(
                icon: Icons.travel_explore_rounded,
                title: 'No hay pedidos disponibles por ahora',
                message: 'Cuando un vecino publique, aparecerá aquí. Desliza hacia abajo para actualizar.',
              )
            else ...[
              if (state.sortedByDistance) ...[
                Text('Ordenados del más cercano al más lejano',
                    style: Theme.of(context).textTheme.labelLarge),
                const SizedBox(height: RtSpacing.s3),
              ],
              for (final order in state.orders) ...[
                RtAvailableOrderCard(
                  key: ValueKey(order.id),
                  zone: order.zone.isEmpty ? 'Trujillo' : order.zone,
                  weight: formatAvailableWeight(order.totalWeightKg),
                  items: formatAvailableItems(order.itemCount),
                  distance: formatDistance(order.distanceKm),
                  timeAgo: formatTimeAgo(order.createdAt),
                  alreadyAccepted: order.alreadyAccepted,
                  thumbnail: order.thumbnailUrl,
                  onClick: () async {
                    final unavailable = await context.push<bool>('/explore/${order.id}');
                    if (mounted && unavailable == true) {
                      showRtSnackbar(context, message: 'Este pedido ya no está disponible');
                    }
                  },
                ),
                const SizedBox(height: RtSpacing.s3),
              ],
            ],
            if (state.loadingMore) const Center(child: Padding(
              padding: EdgeInsets.all(RtSpacing.s3), child: RtSpinner(),
            )),
          ],
        ),
      ),
    );
  }

  /// Solo ambos permisos concedidos habilitan la consulta con coordenadas.
  bool _granted(LocationPermission permission) =>
      permission == LocationPermission.whileInUse || permission == LocationPermission.always;
}
