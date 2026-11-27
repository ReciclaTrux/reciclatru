/// Define el componente reutilizable rt photo thumb del sistema visual compartido por las pantallas.
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Miniatura cuadrada de una foto; usa un ícono cuando no hay imagen y botón de quitar opcional.
class RtPhotoThumb extends StatelessWidget {
  const RtPhotoThumb({
    super.key,
    this.model,
    this.size = 80,
    this.contentDescription,
    this.onRemove,
  });

  final String? model;
  final double size;
  final String? contentDescription;
  final VoidCallback? onRemove;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final isUrl = model != null && (model!.startsWith('http://') || model!.startsWith('https://'));

    return SizedBox(
      width: size,
      height: size,
      child: Stack(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(RtRadii.sm),
            child: Container(
              width: size,
              height: size,
              color: colors.surfaceContainerHighest,
              child: model == null || model!.isEmpty
                  ? Center(
                      child: Icon(
                        Icons.inventory_2_rounded,
                        size: RtSize.iconMd,
                        color: colors.onSurfaceVariant,
                      ),
                    )
                  : isUrl
                      ? Image.network(
                          model!,
                          width: size,
                          height: size,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Center(
                            child: Icon(
                              Icons.broken_image_outlined,
                              color: colors.onSurfaceVariant,
                            ),
                          ),
                        )
                      : Image.file(
                          File(model!),
                          width: size,
                          height: size,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Center(
                            child: Icon(
                              Icons.broken_image_outlined,
                              color: colors.onSurfaceVariant,
                            ),
                          ),
                        ),
            ),
          ),
          if (onRemove != null)
            Positioned(
              top: 4,
              right: 4,
              child: Material(
                color: colors.inverseSurface,
                shape: const CircleBorder(),
                child: InkWell(
                  onTap: onRemove,
                  customBorder: const CircleBorder(),
                  child: Padding(
                    padding: const EdgeInsets.all(3),
                    child: Icon(
                      Icons.close_rounded,
                      size: RtSize.iconXs,
                      color: colors.onInverseSurface,
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
