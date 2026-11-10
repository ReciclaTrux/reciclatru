import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/atoms/rt_spinner.dart';
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/molecules/rt_error_state.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/organisms/rt_action_bar.dart';
import '../../../core/design_system/organisms/rt_top_app_bar.dart';
import '../../../core/design_system/theme/rt_radii.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import '../data/profile_repository.dart';
import '../domain/user_profile.dart';

/// Traducción exacta de CompleteProfileScreen.kt / ProfileViewModel.kt (HU-03).
/// Al completar el perfil, guarda los datos obligatorios y pasa al inicio.
class CompleteProfileScreen extends ConsumerStatefulWidget {
  const CompleteProfileScreen({
    super.key,
    required this.onCompleted,
    required this.onChoosePickupPoint,
  });
  final VoidCallback onCompleted;
  final Future<void> Function() onChoosePickupPoint;

  @override
  ConsumerState<CompleteProfileScreen> createState() => _CompleteProfileScreenState();
}

class _CompleteProfileScreenState extends ConsumerState<CompleteProfileScreen> {
  bool _loading = true;
  bool _saving = false;
  String? _loadError;
  String? _saveError;
  String? _fullNameError;
  String? _phoneError;
  String _role = '';
  String _fullName = '';
  String _phone = '';
  String _reference = '';
  PickupAddress? _pickupAddress;

  @override
  void initState() {
    super.initState();
    _load();
  }

  String _cleanError(Object error) => error.toString().replaceFirst('Exception: ', '');

  Future<void> _load() async {
    setState(() {
      _loading = true;
      _loadError = null;
    });
    try {
      final profile = await ref.read(profileRepositoryProvider).getProfile();
      if (!mounted) return;
      setState(() {
        _role = profile.role;
        _fullName = profile.fullName ?? '';
        _phone = profile.phone ?? '';
        _reference = profile.reference ?? '';
        _pickupAddress = profile.pickupAddress;
        _loading = false;
      });
    } catch (error) {
      if (mounted) {
        setState(() {
          _loading = false;
          _loadError = _cleanError(error);
        });
      }
    }
  }

  Future<void> _choosePickupPoint() async {
    await widget.onChoosePickupPoint();
    if (!mounted) return;
    try {
      final profile = await ref.read(profileRepositoryProvider).getProfile();
      if (mounted) {
        setState(() => _pickupAddress = profile.pickupAddress);
      }
    } catch (_) {}
  }

  Future<void> _save() async {
    if (_saving || _loading) return;
    final name = _fullName.trim();
    final nameError = name.length < 2 ? 'Ingresa tu nombre y apellido.' : null;
    final phoneError = RegExp(r'^9\d{8}$').hasMatch(_phone)
        ? null
        : 'Debe tener 9 dígitos y empezar con 9';

    setState(() {
      _fullNameError = nameError;
      _phoneError = phoneError;
      _saveError = null;
    });

    if (nameError != null || phoneError != null) return;
    setState(() => _saving = true);

    try {
      await ref.read(profileRepositoryProvider).updateProfile(
            fullName: name,
            phone: _phone,
            reference: _role == 'NEIGHBOR' ? _reference : null,
          );
      if (!mounted) return;
      widget.onCompleted();
    } catch (error) {
      if (mounted) setState(() => _saveError = _cleanError(error));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isRecycler = _role == 'RECYCLER';
    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: const PreferredSize(
        preferredSize: Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(title: 'Completa tu perfil'),
        ),
      ),
      bottomNavigationBar: !_loading && _loadError == null
          ? RtActionBar(
              primary: RtButton(
                key: const Key('btn_guardar_perfil'),
                text: _saving ? 'Guardando…' : 'Guardar perfil',
                onClick: _save,
                loading: _saving,
                fullWidth: true,
              ),
            )
          : null,
      body: _loading
          ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
          : _loadError != null
              ? Center(
                  child: RtErrorState(
                    title: 'No pudimos cargar tu perfil',
                    message: _loadError!,
                    onRetry: _load,
                  ),
                )
              : SafeArea(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(RtSpacing.s4),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Text(
                          isRecycler
                              ? 'El vecino verá tu nombre y celular cuando aceptes su pedido.'
                              : 'Así el reciclador sabrá quién eres y cómo llegar a ti.',
                          style: theme.textTheme.bodyLarge?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        if (_saveError != null) ...[
                          RtInlineAlert(
                            key: const Key('alert_perfil_error'),
                            tone: RtInlineAlertTone.error,
                            message: _saveError!,
                          ),
                          const SizedBox(height: RtSpacing.s4),
                        ],
                        RtTextField(
                          key: const Key('input_nombre'),
                          label: 'Nombre y apellido',
                          value: _fullName,
                          placeholder: 'Ana Torres',
                          leadingIcon: Icons.person_rounded,
                          error: _fullNameError,
                          onValueChange: (value) => setState(() {
                            _fullName = value.characters.take(80).toString();
                            _fullNameError = null;
                            _saveError = null;
                          }),
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        RtTextField(
                          key: const Key('input_celular'),
                          label: 'Celular',
                          value: _phone,
                          placeholder: '987654321',
                          helper: isRecycler ? '9 dígitos, empieza con 9' : null,
                          error: _phoneError,
                          leadingIcon: Icons.smartphone_rounded,
                          keyboardType: TextInputType.phone,
                          onValueChange: (value) => setState(() {
                            _phone = value.replaceAll(RegExp(r'[^0-9]'), '');
                            if (_phone.length > 9) _phone = _phone.substring(0, 9);
                            _phoneError = null;
                            _saveError = null;
                          }),
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        if (!isRecycler) ...[
                          Text('Punto de recojo', style: theme.textTheme.labelLarge),
                          const SizedBox(height: RtSpacing.s2),
                          if (_pickupAddress == null) ...[
                            RtButton(
                              text: 'Elegir ubicación en el mapa',
                              onClick: _choosePickupPoint,
                              variant: RtButtonVariant.secondary,
                              icon: Icons.place_rounded,
                              fullWidth: true,
                            ),
                            const SizedBox(height: RtSpacing.s2),
                            Text(
                              'Es donde el reciclador recogerá tu material',
                              style: theme.textTheme.labelMedium?.copyWith(
                                color: theme.colorScheme.onSurfaceVariant,
                              ),
                            ),
                          ] else
                            Row(
                              children: [
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        _pickupAddress!.address.isEmpty
                                            ? 'Punto marcado en el mapa'
                                            : _pickupAddress!.address,
                                        style: theme.textTheme.bodyLarge?.copyWith(
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                      if (_pickupAddress!.district.isNotEmpty)
                                        Text(_pickupAddress!.district),
                                    ],
                                  ),
                                ),
                                RtButton(
                                  text: 'Cambiar',
                                  onClick: _choosePickupPoint,
                                  variant: RtButtonVariant.text,
                                  size: RtButtonSize.compact,
                                ),
                              ],
                            ),
                          const SizedBox(height: RtSpacing.s4),
                          RtTextField(
                            key: const Key('input_referencia'),
                            label: 'Referencia (opcional)',
                            value: _reference,
                            onValueChange: (value) => setState(() {
                              _reference = value.characters.take(150).toString();
                              _saveError = null;
                            }),
                            placeholder: 'Dpto. 302, puerta negra, frente al parque',
                            helper: 'Ayuda al reciclador a encontrar tu puerta',
                            maxLength: 150,
                            multiline: true,
                          ),
                        ] else
                          const RtInlineAlert(
                            tone: RtInlineAlertTone.info,
                            message:
                                'No necesitas registrar una dirección. Para ordenar los pedidos por distancia usaremos la ubicación de tu teléfono, sin guardarla.',
                          ),
                      ],
                    ),
                  ),
                ),
    );
  }
}
