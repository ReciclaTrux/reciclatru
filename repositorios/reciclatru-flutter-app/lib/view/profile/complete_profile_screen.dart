/// Construye la pantalla o navegación de complete profile screen conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:reciclatru/ui/designsystem/atoms/rt_button.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_spinner.dart';
import 'package:reciclatru/ui/designsystem/atoms/rt_text_field.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_error_state.dart';
import 'package:reciclatru/ui/designsystem/molecules/rt_inline_alert.dart';
import 'package:reciclatru/ui/designsystem/organisms/rt_action_bar.dart';
import 'package:reciclatru/ui/designsystem/organisms/rt_top_app_bar.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';
import 'package:reciclatru/viewmodel/profile/profile_view_model.dart';
import 'package:reciclatru/di/session_module.dart';
import 'package:reciclatru/model/session/session.dart';

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
  Future<void> _choosePickupPoint() async {
    await widget.onChoosePickupPoint();
    if (!mounted) return;
    await ref.read(profileViewModelProvider.notifier).onReturnFromPickupPoint();
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(profileViewModelProvider);
    ref.listen(profileViewModelProvider, (previous, next) {
      if (previous?.savedOnce != true && next.savedOnce) widget.onCompleted();
    });
    final theme = Theme.of(context);
    final sessionRole = ref.watch(sessionStoreProvider).currentSession?.role;
    final isRecycler = state.role.toUpperCase() == 'RECYCLER' || sessionRole == UserRole.recycler;
    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      appBar: const PreferredSize(
        preferredSize: Size.fromHeight(RtSize.appbarHeight + RtStroke.divider),
        child: SafeArea(
          bottom: false,
          child: RtTopAppBar(title: 'Completa tu perfil'),
        ),
      ),
      bottomNavigationBar: !state.loading && state.loadError == null
          ? RtActionBar(
              primary: RtButton(
                key: const Key('btn_guardar_perfil'),
                text: state.saving ? 'Guardando…' : 'Guardar perfil',
                onClick: () => ref.read(profileViewModelProvider.notifier).onSave(),
                loading: state.saving,
                fullWidth: true,
              ),
            )
          : null,
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
                        Text(
                          isRecycler
                              ? 'El vecino verá tu nombre y celular cuando aceptes su pedido.'
                              : 'Así el reciclador sabrá quién eres y cómo llegar a ti.',
                          style: theme.textTheme.bodyLarge?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
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
                          helper: isRecycler ? '9 dígitos, empieza con 9' : null,
                          error: state.phoneError,
                          leadingIcon: Icons.smartphone_rounded,
                          keyboardType: TextInputType.phone,
                          onValueChange: ref.read(profileViewModelProvider.notifier).onPhoneChange,
                        ),
                        const SizedBox(height: RtSpacing.s4),
                        if (!isRecycler) ...[
                          Text('Punto de recojo', style: theme.textTheme.labelLarge),
                          const SizedBox(height: RtSpacing.s2),
                          if (state.pickupAddress == null) ...[
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
                                        state.pickupAddress!.address.isEmpty
                                            ? 'Punto marcado en el mapa'
                                            : state.pickupAddress!.address,
                                        style: theme.textTheme.bodyLarge?.copyWith(
                                          fontWeight: FontWeight.w600,
                                        ),
                                      ),
                                      if (state.pickupAddress!.district.isNotEmpty)
                                        Text(state.pickupAddress!.district),
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
                            value: state.reference,
                            onValueChange: ref.read(profileViewModelProvider.notifier).onReferenceChange,
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
