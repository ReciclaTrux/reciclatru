/// Define el componente reutilizable rt photo gallery del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

import 'package:reciclatru/ui/designsystem/theme/rt_spacing.dart';

/// Galería horizontal de las fotos entregadas por el pedido.
class RtPhotoGallery extends StatelessWidget {
  const RtPhotoGallery({super.key, required this.photos});
  final List<String> photos;

  @override
  Widget build(BuildContext context) => SizedBox(
        height: RtSize.thumbLg * 1.4,
        child: ListView.separated(
          scrollDirection: Axis.horizontal,
          itemCount: photos.length,
          separatorBuilder: (_, _) => const SizedBox(width: RtSpacing.s2),
          itemBuilder: (context, index) => ClipRRect(
            borderRadius: BorderRadius.circular(RtRadii.md),
            child: AspectRatio(
              aspectRatio: 4 / 3,
              child: Image.network(
                photos[index],
                fit: BoxFit.cover,
                errorBuilder: (_, _, _) => const Center(child: Icon(Icons.broken_image_outlined)),
              ),
            ),
          ),
        ),
      );
}
