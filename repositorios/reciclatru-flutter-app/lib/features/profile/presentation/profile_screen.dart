import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../core/design_system/atoms/rt_avatar.dart';
import '../../../core/design_system/atoms/rt_badge.dart';
import '../../../core/design_system/atoms/rt_button.dart';
import '../../../core/design_system/atoms/rt_spinner.dart';
import '../../../core/design_system/atoms/rt_text_field.dart';
import '../../../core/design_system/molecules/rt_error_state.dart';
import '../../../core/design_system/molecules/rt_inline_alert.dart';
import '../../../core/design_system/molecules/rt_snackbar.dart';
import '../../../core/design_system/organisms/rt_top_app_bar.dart';
import '../../../core/design_system/theme/rt_radii.dart';
import '../../../core/design_system/theme/rt_spacing.dart';
import '../../auth/presentation/auth_provider.dart';
import '../data/profile_repository.dart';
import '../domain/user_profile.dart';

/// Traducción exacta de ProfileContent / ProfileViewModel de Android (HU-03).
/// Pantalla de edición de perfil para vecinos y recicladores.
class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  bool _loading = true;
  bool _saving = false;
  String? _loadError;
  String? _saveError;
  String? _fullNameError;
  String? _phoneError;

  String _email = '';
  String _role = '';
  String _savedFullName = '';
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
        _email = profile.email;
        _role = profile.role;
        _savedFullName = profile.fullName ?? '';
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
    await context.push<bool>('/pickup-location', extra: _pickupAddress);
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
      final updated = await ref.read(profileRepositoryProvider).updateProfile(
            fullName: name,
            phone: _phone,
            reference: _role == 'NEIGHBOR' ? _reference : null,
          );
      if (!mounted) return;
      setState(() {
        _savedFullName = updated.fullName ?? name;
        _fullName = updated.fullName ?? name;
        _phone = updated.phone ?? _phone;
        _saving = false;
      });
      showRtSnackbar(context, message: 'Guardamos tu perfil.');
    } catch (error) {
      if (mounted) setState(() => _saveError = _cleanError(error));
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }

  Future<void> _logout() async {
    await ref.read(authRepositoryProvider).logout();
    if (mounted) {
      context.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final isNeighbor = _role == 'NEIGHBOR';

    return Scaffold(
      backgroundColor: theme.colorScheme.surface,
      appBar: PreferredSize(
        preferredSize: const Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(
            title: 'Perfil',
            onBack: () => context.canPop() ? context.pop() : context.go('/home'),
          ),
        ),
      ),
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
                        // Profile Header
                        Row(
                          children: [
                            RtAvatar(
                              name: _savedFullName.isNotEmpty ? _savedFullName : _email,
                            ),
                            const SizedBox(width: RtSpacing.s3),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    _savedFullName.isNotEmpty ? _savedFullName : 'Sin nombre',
                                    style: theme.textTheme.bodyLarge?.copyWith(
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                  Text(
                                    _email,
                                    style: theme.textTheme.bodyMedium?.copyWith(
                                      color: theme.colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            if (_role.isNotEmpty)
                              RtBadge(
                                text: isNeighbor ? 'Vecino' : 'Reciclador',
                                icon: isNeighbor
                                    ? Icons.home_rounded
                                    : Icons.recycling_rounded,
                              ),
                          ],
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
                          helper: !isNeighbor
                              ? 'Si lo cambias, los vecinos con pedidos activos verán el número nuevo'
                              : null,
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
                        if (isNeighbor) ...[
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
                        ],
                        RtTextField(
                          key: const Key('input_correo_rol'),
                          label: 'Correo y rol',
                          value: '$_email · ${isNeighbor ? "Vecino" : "Reciclador"}',
                          readOnly: true,
                          onValueChange: (_) {},
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        RtButton(
                          key: const Key('btn_guardar_cambios'),
                          text: _saving ? 'Guardando…' : 'Guardar cambios',
                          onClick: _save,
                          variant: RtButtonVariant.secondary,
                          loading: _saving,
                          fullWidth: true,
                        ),
                        const SizedBox(height: RtSpacing.s3),
                        Center(
                          child: RtButton(
                            key: const Key('btn_cerrar_sesion'),
                            text: 'Cerrar sesión',
                            onClick: _logout,
                            variant: RtButtonVariant.text,
                            icon: Icons.logout_rounded,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
    );
  }
}
