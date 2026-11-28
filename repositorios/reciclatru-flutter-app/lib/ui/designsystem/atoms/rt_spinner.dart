/// Define el componente reutilizable rt spinner del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_colors.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Tamaño de [RtSpinner].
enum RtSpinnerSize {
  /// 20 px, color del texto: dentro de un botón o de la lista.
  small,

  /// 32 px, `primary`: carga inicial centrada en la pantalla.
  large,
}

/// Indicador circular de carga indeterminada.
class RtSpinner extends StatelessWidget {
  const RtSpinner({super.key, this.size = RtSpinnerSize.small, this.color});

  final RtSpinnerSize size;

  /// Color del trazo. Por defecto, `primary` en [RtSpinnerSize.large] y el
  /// color del texto en [RtSpinnerSize.small].
  final Color? color;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final extra = Theme.of(context).extension<RtExtraColors>()!;
    final isLarge = size == RtSpinnerSize.large;
    final diameter = isLarge ? RtSize.spinnerLarge : RtSize.spinnerSmall;
    final strokeWidth = isLarge ? RtStroke.spinnerLarge : RtStroke.spinnerSmall;
    final resolvedColor =
        color ??
        (isLarge
            ? colors.primary
            : DefaultTextStyle.of(context).style.color ?? extra.inkSubtle);

    return SizedBox(
      width: diameter,
      height: diameter,
      child: CircularProgressIndicator(
        strokeWidth: strokeWidth,
        valueColor: AlwaysStoppedAnimation<Color>(resolvedColor),
        semanticsLabel: 'Cargando',
      ),
    );
  }
}
