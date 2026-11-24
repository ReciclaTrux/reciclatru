import 'dart:io';

import 'package:flutter/material.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import 'rt_spinner.dart';

/// Tamaño de [RtPhotoThumb].
enum RtPhotoThumbSize {
  /// 56 px: tarjetas de lista (HU-06, HU-10).
  small,

  /// 80 px: selector de fotos del formulario (HU-09).
  medium,

  /// 112 px: galería del detalle (HU-11).
  large,
}

/// Miniatura cuadrada de una foto del pedido.
///
/// Estados: con foto, sin foto (`inventory_2`), no cargó (`broken_image`),
/// quitar (solo si recibe [onRemove]) y subiendo (capa `scrim` con
/// [RtSpinner]). La foto se recorta cuadrada y centrada.
///
/// La imagen se carga con `Image.network`/`Image.file` y siempre tiene un
/// recuadro de reemplazo; no se agregó `cached_network_image` porque el plan
/// solo permite las dependencias del tema y del catálogo.
class RtPhotoThumb extends StatelessWidget {
  const RtPhotoThumb({
    super.key,
    this.url,
    this.file,
    this.size = RtPhotoThumbSize.small,
    this.onRemove,
    this.uploading = false,
    this.removeDescription = 'Quitar foto',
  });

  /// URL de la foto. Se ignora si llega [file].
  final String? url;

  /// Archivo local todavía sin subir.
  final File? file;

  final RtPhotoThumbSize size;

  /// Si existe, dibuja el botón circular de quitar arriba a la derecha.
  final VoidCallback? onRemove;

  /// Mientras sube, tapa la foto con el `scrim` y un [RtSpinner].
  final bool uploading;

  /// Descripción accesible del botón de quitar ("Quitar foto 2").
  final String removeDescription;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final extra = theme.extension<RtExtraColors>()!;
    final side = switch (size) {
      RtPhotoThumbSize.small => RtSize.thumbSm,
      RtPhotoThumbSize.medium => RtSize.thumbMd,
      RtPhotoThumbSize.large => RtSize.thumbLg,
    };

    final image = _buildImage(extra);

    return SizedBox(
      width: side,
      height: side,
      child: Stack(
        clipBehavior: Clip.none,
        children: <Widget>[
          Positioned.fill(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(RtRadii.sm),
              child: ColoredBox(
                color: colors.surfaceContainerHighest,
                child: image,
              ),
            ),
          ),
          if (uploading)
            Positioned.fill(
              child: ClipRRect(
                borderRadius: BorderRadius.circular(RtRadii.sm),
                child: ColoredBox(
                  color: colors.scrim,
                  child: Center(child: RtSpinner(color: colors.onPrimary)),
                ),
              ),
            ),
          if (onRemove != null)
            Positioned(
              top: RtSpacing.s1 - (RtSize.touchTarget - RtSize.thumbRemove) / 2,
              right:
                  RtSpacing.s1 - (RtSize.touchTarget - RtSize.thumbRemove) / 2,
              width: RtSize.touchTarget,
              height: RtSize.touchTarget,
              child: Center(
                child: _RemoveButton(
                  description: removeDescription,
                  onClick: onRemove!,
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildImage(RtExtraColors extra) {
    final placeholderColor = extra.inkSubtle;
    final glyphSize = switch (size) {
      RtPhotoThumbSize.small || RtPhotoThumbSize.medium => RtSize.iconMd,
      RtPhotoThumbSize.large => RtSize.stateIconGlyph,
    };

    Widget placeholder(IconData icon, String label) {
      return Semantics(
        image: true,
        label: label,
        child: ExcludeSemantics(
          child: Center(
            child: Icon(icon, size: glyphSize, color: placeholderColor),
          ),
        ),
      );
    }

    if (file != null) {
      return Image.file(
        file!,
        fit: BoxFit.cover,
        width: double.infinity,
        height: double.infinity,
        errorBuilder: (_, _, _) =>
            placeholder(Icons.broken_image_rounded, 'La foto no cargó'),
      );
    }
    if (url != null) {
      return Image.network(
        url!,
        fit: BoxFit.cover,
        width: double.infinity,
        height: double.infinity,
        errorBuilder: (_, _, _) =>
            placeholder(Icons.broken_image_rounded, 'La foto no cargó'),
      );
    }
    return placeholder(Icons.inventory_2_rounded, 'Sin foto');
  }
}

class _RemoveButton extends StatelessWidget {
  const _RemoveButton({required this.description, required this.onClick});

  final String description;
  final VoidCallback onClick;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Semantics(
      button: true,
      label: description,
      child: Material(
        color: colors.inverseSurface,
        shape: const CircleBorder(),
        child: InkWell(
          onTap: onClick,
          customBorder: const CircleBorder(),
          child: SizedBox(
            width: RtSize.thumbRemove,
            height: RtSize.thumbRemove,
            child: Icon(
              Icons.close_rounded,
              size: RtSize.iconSm,
              color: colors.onInverseSurface,
            ),
          ),
        ),
      ),
    );
  }
}
