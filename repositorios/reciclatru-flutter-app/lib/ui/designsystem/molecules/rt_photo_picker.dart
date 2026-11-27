/// Define el componente reutilizable rt photo picker del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';
import '../atoms/rt_photo_thumb.dart';
import '../theme/rt_spacing.dart';

/// Selector visual de imágenes con recuadro "Agregar foto" e indicación de cantidad igual a Kotlin.
class RtPhotoPicker extends StatelessWidget {
  const RtPhotoPicker({
    required this.photos,
    required this.onAdd,
    required this.onRemove,
    this.maxPhotos = 3,
    this.enabled = true,
    super.key,
  });

  final List<String> photos;
  final VoidCallback onAdd;
  final ValueChanged<int> onRemove;
  final int maxPhotos;
  final bool enabled;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final isFull = photos.length >= maxPhotos;
    final canAdd = enabled && !isFull;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Fotos (opcional)',
          style: Theme.of(context).textTheme.labelLarge?.copyWith(
            color: colors.onSurface,
          ),
        ),
        const SizedBox(height: RtSpacing.s2),
        Row(
          children: [
            for (var index = 0; index < photos.length; index++) ...[
              RtPhotoThumb(
                model: photos[index],
                size: 80,
                onRemove: enabled ? () => onRemove(index) : null,
              ),
              const SizedBox(width: RtSpacing.s2),
            ],
            Material(
              color: canAdd ? colors.surface : colors.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(RtRadii.sm),
              child: InkWell(
                onTap: canAdd ? onAdd : null,
                borderRadius: BorderRadius.circular(RtRadii.sm),
                child: Container(
                  width: RtSize.thumbMd,
                  height: RtSize.thumbMd,
                  decoration: BoxDecoration(
                    border: Border.all(
                      color: canAdd ? colors.primary : colors.outlineVariant,
                      width: RtStroke.control,
                    ),
                    borderRadius: BorderRadius.circular(RtRadii.sm),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.add_a_photo_rounded,
                        size: RtSize.iconMd,
                        color: canAdd ? colors.primary : colors.onSurfaceVariant,
                      ),
                      const SizedBox(height: RtSpacing.s1),
                      Text(
                        isFull ? 'Máximo $maxPhotos' : 'Agregar foto',
                        style: Theme.of(context).textTheme.labelSmall?.copyWith(
                          color: canAdd ? colors.primary : colors.onSurfaceVariant,
                          fontWeight: FontWeight.w600,
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: RtSpacing.s2),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Toma una foto o elígela de tu galería',
              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
            Text(
              '${photos.length}/$maxPhotos',
              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                color: colors.onSurfaceVariant,
              ),
            ),
          ],
        ),
      ],
    );
  }
}
