import 'dart:io';

import 'package:flutter/material.dart';

import '../atoms/rt_photo_thumb.dart';
import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_typography.dart';

/// Foto del selector: una URL ya subida, un archivo local o una que está
/// subiendo.
class RtPhoto {
  const RtPhoto({this.url, this.file, this.uploading = false});

  final String? url;
  final File? file;
  final bool uploading;
}

/// Hilera de hasta [max] miniaturas con un recuadro para agregar foto desde la
/// cámara o la galería (HU-09).
///
/// [onAdd] abre la hoja con "Tomar foto" y "Elegir de la galería": la hoja y el
/// permiso los maneja la pantalla, no la molécula. Al llegar a [max] el
/// recuadro se desactiva y dice "Máximo 3". Si se cancela la cámara o la
/// galería, no cambia nada y no se muestra ningún mensaje.
class RtPhotoPicker extends StatelessWidget {
  const RtPhotoPicker({
    super.key,
    required this.photos,
    required this.onAdd,
    required this.onRemove,
    this.max = 3,
  });

  final List<RtPhoto> photos;
  final VoidCallback onAdd;
  final ValueChanged<int> onRemove;
  final int max;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final helperStyle = theme.textTheme.labelMedium!.copyWith(
      color: colors.onSurfaceVariant,
    );
    final canAdd = photos.length < max;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Text(
          'Fotos (opcional)',
          style: theme.textTheme.labelLarge!.copyWith(color: colors.onSurface),
        ),
        const SizedBox(height: RtSpacing.fieldLabelGap),
        Wrap(
          spacing: RtSpacing.s3,
          runSpacing: RtSpacing.s3,
          children: <Widget>[
            for (var index = 0; index < photos.length; index++)
              RtPhotoThumb(
                url: photos[index].url,
                file: photos[index].file,
                size: RtPhotoThumbSize.medium,
                uploading: photos[index].uploading,
                onRemove: photos[index].uploading
                    ? null
                    : () => onRemove(index),
                removeDescription: 'Quitar foto ${index + 1}',
              ),
            _AddPhotoTile(enabled: canAdd, max: max, onAdd: onAdd),
          ],
        ),
        const SizedBox(height: RtSpacing.fieldLabelGap),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: <Widget>[
            Flexible(
              child: Text(
                'Toma una foto o elígela de tu galería',
                style: helperStyle,
              ),
            ),
            const SizedBox(width: RtSpacing.s2),
            Text('${photos.length}/$max', style: helperStyle),
          ],
        ),
      ],
    );
  }
}

class _AddPhotoTile extends StatelessWidget {
  const _AddPhotoTile({
    required this.enabled,
    required this.max,
    required this.onAdd,
  });

  final bool enabled;
  final int max;
  final VoidCallback onAdd;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final extra = theme.extension<RtExtraColors>()!;
    final label = enabled ? 'Agregar foto' : 'Máximo $max';
    final foreground = enabled ? colors.primary : extra.disabledInk;
    final background = enabled ? colors.surface : extra.disabledBg;

    return Semantics(
      button: true,
      enabled: enabled,
      label: label,
      child: Material(
        color: background,
        borderRadius: BorderRadius.circular(RtRadii.sm),
        clipBehavior: Clip.antiAlias,
        child: InkWell(
          onTap: enabled ? onAdd : null,
          child: CustomPaint(
            painter: _DashedBorderPainter(
              color: enabled ? colors.outline : colors.outlineVariant,
            ),
            child: SizedBox(
              width: RtSize.thumbMd,
              height: RtSize.thumbMd,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: <Widget>[
                  Icon(
                    Icons.add_a_photo_rounded,
                    size: RtSize.iconMd,
                    color: foreground,
                  ),
                  const SizedBox(height: RtSpacing.photoAddGap),
                  Text(
                    label,
                    textAlign: TextAlign.center,
                    style: RtTypography.photoAddLabel.copyWith(
                      color: foreground,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Borde discontinuo de 1.5 px con las esquinas de `radius-sm`
/// (`.rt-thumb--add`).
class _DashedBorderPainter extends CustomPainter {
  const _DashedBorderPainter({required this.color});

  final Color color;

  static const double _dash = 4;
  static const double _gap = 3;

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = RtStroke.control;
    final path = Path()
      ..addRRect(
        RRect.fromRectAndRadius(
          Offset.zero & size,
          const Radius.circular(RtRadii.sm),
        ),
      );
    for (final metric in path.computeMetrics()) {
      var distance = 0.0;
      while (distance < metric.length) {
        final end = (distance + _dash).clamp(0.0, metric.length);
        canvas.drawPath(metric.extractPath(distance, end), paint);
        distance = end + _gap;
      }
    }
  }

  @override
  bool shouldRepaint(_DashedBorderPainter oldDelegate) =>
      oldDelegate.color != color;
}
