/// Construye la pantalla o navegación de profile screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_avatar.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_badge.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_spinner.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_text_field.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_error_state.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_inline_alert.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_snackbar.dart';
import 'package:reciclatru/ui/designsystem/organisms/rt_top_app_bar.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';
import 'package:reciclatru/di/auth_module.dart';
import 'package:reciclatru/view/profile/profile_fields.dart';
import 'package:reciclatru/viewmodel/profile/profile_view_model.dart';

/// Traducción exacta de ProfileContent / ProfileViewModel de Android (HU-03).
/// Pantalla de edición de perfil para vecinos y recicladores.
class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  Future<void> _choosePickupPoint() async {
    await context.push<bool>('/pickup-location');
    if (!mounted) return;
    await ref.read(profileViewModelProvider.notifier).onReturnFromPickupPoint();
  }

  Future<void> _logout() async {
    await ref.read(authRepositoryProvider).logout();
    if (mounted) {
      context.go('/login');
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(profileViewModelProvider);
    ref.listen(profileViewModelProvider, (previous, next) {
      if (next.message != null && previous?.message != next.message) {
        showRtSnackbar(context, message: next.message!);
        ref.read(profileViewModelProvider.notifier).onMessageShown();
      }
    });
    final theme = Theme.of(context);
    final isNeighbor = state.role == 'NEIGHBOR';

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
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
      body: state.loading
          ? const Center(child: RtSpinner(size: RtSpinnerSize.large))
          : state.loadError != null
              ? Center(
                  child: RtErrorState(
                    title: 'No pudimos cargar tu perfil',
                    message: state.loadError!,
                    onRetry: () => ref.read(profileViewModelProvider.notifier).onRetry(),
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
                              name: state.savedFullName.isNotEmpty ? state.savedFullName : state.email,
                            ),
                            const SizedBox(width: RtSpacing.s3),
                            Expanded(
                              child: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  Text(
                                    state.savedFullName.isNotEmpty ? state.savedFullName : 'Sin nombre',
                                    style: theme.textTheme.bodyLarge?.copyWith(
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                  Text(
                                    state.email,
                                    style: theme.textTheme.bodyMedium?.copyWith(
                                      color: theme.colorScheme.onSurfaceVariant,
                                    ),
                                  ),
                                ],
                              ),
                            ),
                            if (state.role.isNotEmpty)
                              RtBadge(
                                text: isNeighbor ? 'Vecino' : 'Reciclador',
                                icon: isNeighbor
                                    ? Icons.home_rounded
                                    : Icons.recycling_rounded,
                              ),
                          ],
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        if (state.saveError != null) ...[
                          RtInlineAlert(
                            key: const Key('alert_perfil_error'),
                            tone: RtInlineAlertTone.error,
                            message: state.saveError!,
                          ),
                          const SizedBox(height: RtSpacing.s4),
                        ],
                        RtTextField(
                          key: const Key('input_nombre'),
                          label: 'Nombre y apellido',
                          value: state.fullName,
                          placeholder: 'Ana Torres',
                          leadingIcon: Icons.person_rounded,
                          error: state.fullNameError,
                          onValueChange: ref.read(profileViewModelProvider.notifier).onFullNameChange,
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        RtTextField(
                          key: const Key('input_celular'),
                          label: 'Celular',
                          value: state.phone,
                          placeholder: '987654321',
                          helper: !isNeighbor
                              ? 'Si lo cambias, los vecinos con pedidos activos verán el número nuevo'
                              : null,
                          error: state.phoneError,
                          leadingIcon: Icons.smartphone_rounded,
                          keyboardType: TextInputType.phone,
                          onValueChange: ref.read(profileViewModelProvider.notifier).onPhoneChange,
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        if (isNeighbor) ...[
                          PickupPointSection(
                            address: state.pickupAddress,
                            reference: state.reference,
                            onChoose: _choosePickupPoint,
                          ),
                          const SizedBox(height: RtSpacing.s4),
                        ],
                        RtTextField(
                          key: const Key('input_correo_rol'),
                          label: 'Correo y rol',
                          value: '${state.email} · ${isNeighbor ? "Vecino" : "Reciclador"}',
                          readOnly: true,
                          onValueChange: (_) {},
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        RtButton(
                          key: const Key('btn_guardar_cambios'),
                          text: state.saving ? 'Guardando…' : 'Guardar cambios',
                          onClick: () => ref.read(profileViewModelProvider.notifier).onSave(),
                          variant: RtButtonVariant.secondary,
                          loading: state.saving,
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
