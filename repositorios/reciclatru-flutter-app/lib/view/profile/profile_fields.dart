/// Construye la pantalla o navegación de profile fields conservando el flujo de usuario de Kotlin.
import 'package:flutter/material.dart';

import '../../model/profile/pickup_location.dart';
import '../../model/session/session.dart';
import '../../ui/designsystem/atoms/rt_button.dart';
import '../../ui/designsystem/atoms/rt_text_field.dart';
import '../../ui/designsystem/theme/rt_spacing.dart';
import '../../ui/designsystem/theme/rt_typography.dart';
import '../../viewmodel/profile/profile_ui_state.dart';

/// Texto de rol mostrado en el perfil, equivalente a `UserRole.label()` de Kotlin.
String userRoleLabel(UserRole role) => switch (role) {
  UserRole.neighbor => 'Vecino',
  UserRole.recycler => 'Reciclador',
};

/// Campo compartido de nombre y apellido.
class FullNameField extends StatelessWidget {
  const FullNameField({required this.state, required this.onChange, super.key});
  final ProfileUiState state;
  final ValueChanged<String> onChange;
  @override
  Widget build(BuildContext context) => RtTextField(
    label: 'Nombre y apellido',
    value: state.fullName,
    onValueChange: onChange,
    placeholder: 'Ana Torres',
    error: state.fullNameError,
    leadingIcon: Icons.person_rounded,
  );
}

/// Campo compartido de celular con la ayuda específica del formulario.
class PhoneField extends StatelessWidget {
  const PhoneField({required this.state, required this.onChange, this.helper, super.key});
  final ProfileUiState state;
  final ValueChanged<String> onChange;
  final String? helper;
  @override
  Widget build(BuildContext context) => RtTextField(
    label: 'Celular',
    value: state.phone,
    onValueChange: onChange,
    placeholder: '987654321',
    helper: helper,
    error: state.phoneError,
    leadingIcon: Icons.smartphone_rounded,
    keyboardType: TextInputType.phone,
  );
}

/// Muestra punto de recojo, su referencia y la acción para elegir o cambiar mapa.
class PickupPointSection extends StatelessWidget {
  const PickupPointSection({
    required this.address,
    required this.reference,
    required this.onChoose,
    super.key,
  });

  final PickupAddress? address;
  final String? reference;
  final VoidCallback onChoose;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final details = [
      address?.district ?? '',
      if (reference?.isNotEmpty == true) reference!,
    ].where((value) => value.isNotEmpty).join(' · ');

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('Punto de recojo', style: theme.textTheme.labelLarge),
        const SizedBox(height: RtSpacing.s2),
        if (address == null) ...[
          RtButton(
            key: const Key('btn_elegir_ubicacion'),
            text: 'Elegir ubicación en el mapa',
            onClick: onChoose,
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
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      address!.address.isEmpty
                          ? 'Punto marcado en el mapa'
                          : address!.address,
                      style: RtTypography.bodyStrong,
                    ),
                    if (details.isNotEmpty)
                      Text(
                        details,
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                  ],
                ),
              ),
              RtButton(
                key: const Key('btn_cambiar_ubicacion'),
                text: 'Cambiar',
                onClick: onChoose,
                variant: RtButtonVariant.text,
                size: RtButtonSize.compact,
              ),
            ],
          ),
      ],
    );
  }
}
