/// Construye la pantalla o navegación de location screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../model/profile/geo_point.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/atoms/rt_spinner.dart';
import '../../ui/designsystem/atoms/rt_text_field.dart';
import '../../ui/designsystem/molecules/rt_inline_alert.dart';
import '../../ui/designsystem/organisms/rt_action_bar.dart';
import '../../ui/designsystem/organisms/rt_map_picker.dart';
import '../../ui/designsystem/organisms/rt_top_app_bar.dart';
import '../../ui/designsystem/molecules/rt_snackbar.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../viewmodel/profile/location_ui_state.dart';
import '../../viewmodel/profile/location_view_model.dart';

/// Pantalla única de ubicación con los pasos de mapa, dirección y guardado de Kotlin.
class LocationScreen extends ConsumerStatefulWidget {
  const LocationScreen({super.key});

  @override
  ConsumerState<LocationScreen> createState() => _LocationScreenState();
}

class _LocationScreenState extends ConsumerState<LocationScreen> {
  @override
  void initState() {
    super.initState();
    ref.read(locationViewModelProvider.notifier).onStart();
  }

  /// Vuelve al mapa dentro de la pantalla o sale si ya está en el primer paso.
  void _onBack(LocationUiState state) {
    if (state.step == LocationStep.address) {
      ref.read(locationViewModelProvider.notifier).onBackToMap();
    } else {
      context.pop(true);
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(locationViewModelProvider);
    final viewModel = ref.read(locationViewModelProvider.notifier);
    final theme = Theme.of(context);

    // El paso Dirección consume el botón Atrás para volver al mapa, como BackHandler.
    return PopScope<Object?>(
      canPop: state.step != LocationStep.address,
      onPopInvokedWithResult: (didPop, _) {
        if (!didPop && state.step == LocationStep.address) {
          viewModel.onBackToMap();
        }
      },
      child: Scaffold(
        backgroundColor: theme.scaffoldBackgroundColor,
        appBar: PreferredSize(
          preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
          child: SafeArea(
            bottom: false,
            child: RtTopAppBar(
              title: switch (state.step) {
                LocationStep.map => 'Punto de recojo',
                LocationStep.address => 'Confirma la dirección',
                LocationStep.saved => 'Punto de recojo',
              },
              onBack: () => _onBack(state),
            ),
          ),
        ),
        body: SafeArea(
          child: switch (state.step) {
            LocationStep.map => _MapStep(state: state, viewModel: viewModel),
            LocationStep.address => _AddressStep(state: state, viewModel: viewModel),
            LocationStep.saved => _SavedStep(state: state),
          },
        ),
      ),
    );
  }
}

/// Paso del mapa y búsqueda de direcciones.
class _MapStep extends StatelessWidget {
  const _MapStep({required this.state, required this.viewModel});
  final LocationUiState state;
  final LocationViewModel viewModel;

  @override
  Widget build(BuildContext context) => SingleChildScrollView(
        padding: const EdgeInsets.all(RtSpacing.s4),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            if (!state.geocodingAvailable) ...[
              const RtInlineAlert(
                tone: RtInlineAlertTone.info,
                message: 'Tu teléfono no puede buscar direcciones. Mueve el mapa a mano.',
              ),
              const SizedBox(height: RtSpacing.s4),
            ],
            RtMapPicker(
              key: const Key('map_punto_recojo'),
              initialCenter: _toMapPoint(state.initialCenter),
              center: state.center == null ? null : _toMapPoint(state.center!),
              onConfirm: viewModel.onMapConfirmed,
              searchEnabled: state.geocodingAvailable,
              onSearch: viewModel.onSearch,
              onSearchQueryChanged: viewModel.onSearchQueryChanged,
              searchSuggestions: state.suggestions.map((item) => item.label).toList(),
              searchLoading: state.searching,
              onSuggestionSelected: (label) {
                final selected = state.suggestions.where((item) => item.label == label);
                if (selected.isNotEmpty) {
                  viewModel.onSuggestionSelected(selected.first);
                  showRtSnackbar(context, message: 'Dirección actualizada en el mapa');
                }
              },
              searchError: state.searchError,
            ),
          ],
        ),
      );
}

/// Paso de confirmación de dirección dentro de la misma pantalla de Kotlin.
class _AddressStep extends StatelessWidget {
  const _AddressStep({required this.state, required this.viewModel});
  final LocationUiState state;
  final LocationViewModel viewModel;

  @override
  Widget build(BuildContext context) => Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              padding: const EdgeInsets.all(RtSpacing.s4),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
                  if (state.resolvingAddress) ...[
                    const Row(
                      children: [
                        RtSpinner(),
                        SizedBox(width: RtSpacing.s2),
                        Text('Buscando la dirección del pin.'),
                      ],
                    ),
                    const SizedBox(height: RtSpacing.s4),
                  ],
                  if (state.addressError != null) ...[
                    RtInlineAlert(
                      tone: RtInlineAlertTone.info,
                      message: state.addressError!,
                    ),
                    const SizedBox(height: RtSpacing.s4),
                  ],
                  RtTextField(
                    key: const Key('input_direccion'),
                    label: 'Dirección',
                    value: state.address,
                    onValueChange: viewModel.onAddressChange,
                    placeholder: 'Av. Larco 1250',
                    helper: 'Puedes corregir el texto; el punto del mapa no cambia',
                    error: state.addressError,
                  ),
                  const SizedBox(height: RtSpacing.s4),
                  RtTextField(
                    key: const Key('input_distrito'),
                    label: 'Distrito',
                    value: state.district,
                    onValueChange: viewModel.onDistrictChange,
                    placeholder: 'Víctor Larco Herrera',
                    error: state.districtError,
                  ),
                  const SizedBox(height: RtSpacing.s4),
                  const RtInlineAlert(
                    tone: RtInlineAlertTone.info,
                    message: 'Este punto se copiará a los pedidos que publiques desde ahora.',
                  ),
                  if (state.saveError != null) ...[
                    const SizedBox(height: RtSpacing.s4),
                    RtInlineAlert(
                      tone: RtInlineAlertTone.error,
                      message: state.saveError!,
                    ),
                  ],
                ],
              ),
            ),
          ),
          RtActionBar(
            primary: RtButton(
              key: const Key('btn_guardar_ubicacion'),
              text: state.saving ? 'Guardando…' : 'Guardar ubicación',
              onClick: viewModel.onSave,
              loading: state.saving,
              fullWidth: true,
            ),
          ),
        ],
      );
}

/// Paso final después de que la API confirmó la ubicación guardada.
class _SavedStep extends StatelessWidget {
  const _SavedStep({required this.state});
  final LocationUiState state;

  @override
  Widget build(BuildContext context) {
    final location = state.savedLocation;
    return Column(
      children: [
        Expanded(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(RtSpacing.s4),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                const RtInlineAlert(
                  tone: RtInlineAlertTone.success,
                  title: 'Ubicación guardada',
                  message: 'Guardamos tu punto de recojo.',
                ),
                if (location != null) ...[
                  const SizedBox(height: RtSpacing.s4),
                  Text('${location.address}, ${location.district}'),
                ],
              ],
            ),
          ),
        ),
        RtActionBar(
          primary: RtButton(
            key: const Key('btn_listo_ubicacion'),
            text: 'Listo',
            onClick: () => context.pop(true),
            fullWidth: true,
          ),
        ),
      ],
    );
  }
}

/// Convierte el modelo geográfico al tipo recibido por el organismo del mapa.
RtLatLng _toMapPoint(GeoPoint point) =>
    RtLatLng(latitude: point.latitude, longitude: point.longitude);
