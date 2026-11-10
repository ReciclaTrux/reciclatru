import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/organisms/rt_action_bar.dart';
import '../../../core/design_system/organisms/rt_top_app_bar.dart';
import '../../../core/design_system/theme/rt_radii.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import '../data/profile_repository.dart';
import '../domain/user_profile.dart';

/// P07: Confirmar dirección (HU-04).
/// Traducción exacta de AddressStep / LocationViewModel de Android.
class LocationConfirmationScreen extends ConsumerStatefulWidget {
  const LocationConfirmationScreen({
    super.key,
    required this.point,
  });

  final PickupAddress point;

  @override
  ConsumerState<LocationConfirmationScreen> createState() =>
      _LocationConfirmationScreenState();
}

class _LocationConfirmationScreenState
    extends ConsumerState<LocationConfirmationScreen> {
  late String _address;
  late String _district;

  bool _saving = false;
  String? _addressError;
  String? _districtError;
  String? _saveError;

  @override
  void initState() {
    super.initState();
    _address = widget.point.address;
    _district = widget.point.district;
  }

  Future<void> _save() async {
    if (_saving) return;

    final address = _address.trim();
    final district = _district.trim();

    final addressError = address.isEmpty ? 'Ingresa la dirección.' : null;
    final districtError = district.isEmpty ? 'Ingresa el distrito.' : null;

    setState(() {
      _addressError = addressError;
      _districtError = districtError;
      _saveError = null;
    });

    if (addressError != null || districtError != null) return;

    setState(() => _saving = true);

    try {
      final saved = await ref
          .read(profileRepositoryProvider)
          .updatePickupPoint(
            latitude: widget.point.latitude,
            longitude: widget.point.longitude,
            address: address,
            district: district,
          );

      if (!mounted) return;
      context.pop(saved);
    } catch (error) {
      if (!mounted) return;
      setState(() {
        _saveError = error.toString().replaceFirst('Exception: ', '');
      });
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(
          RtSize.appbarHeight + RtStroke.divider,
        ),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(
            title: 'Confirma la dirección',
            onBack: () => context.pop(),
          ),
        ),
      ),
      bottomNavigationBar: RtActionBar(
        primary: RtButton(
          key: const Key('btn_guardar_ubicacion'),
          text: _saving ? 'Guardando…' : 'Guardar ubicación',
          fullWidth: true,
          loading: _saving,
          enabled: !_saving,
          onClick: _save,
        ),
      ),
      body: SafeArea(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(RtSpacing.s4),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (_saveError != null) ...[
                RtInlineAlert(
                  key: const Key('alert_error_ubicacion'),
                  tone: RtInlineAlertTone.error,
                  message: _saveError!,
                ),
                const SizedBox(height: RtSpacing.s4),
              ],
              RtTextField(
                key: const Key('input_direccion'),
                label: 'Dirección',
                value: _address,
                placeholder: 'Av. Larco 1250',
                helper: 'Puedes corregir el texto; el punto del mapa no cambia',
                error: _addressError,
                leadingIcon: Icons.location_on_outlined,
                onValueChange: (value) {
                  setState(() {
                    _address = value;
                    _addressError = null;
                    _saveError = null;
                  });
                },
              ),
              const SizedBox(height: RtSpacing.s4),
              RtTextField(
                key: const Key('input_distrito'),
                label: 'Distrito',
                value: _district,
                placeholder: 'Víctor Larco Herrera',
                error: _districtError,
                leadingIcon: Icons.map_outlined,
                onValueChange: (value) {
                  setState(() {
                    _district = value;
                    _districtError = null;
                    _saveError = null;
                  });
                },
              ),
              const SizedBox(height: RtSpacing.s4),
              const RtInlineAlert(
                key: Key('alert_ubicacion_info'),
                tone: RtInlineAlertTone.info,
                message:
                    'Este punto se copiará a los pedidos que publiques desde ahora.',
              ),
            ],
          ),
        ),
      ),
    );
  }
}
