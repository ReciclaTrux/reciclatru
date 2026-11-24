import 'package:flutter/material.dart';

import '../atoms/rt_icon_button.dart';
import '../atoms/rt_photo_thumb.dart';
import '../theme/rt_spacing.dart';

/// Galería horizontal de las fotos del pedido (HU-11).
///
/// Sin fotos, la sección no aparece. Tocar una foto llama a [onOpen] con su
/// posición; la pantalla abre el visor con [RtPhotoGallery.showViewer]. Una
/// foto que no carga muestra el recuadro de reemplazo de [RtPhotoThumb].
class RtPhotoGallery extends StatelessWidget {
  const RtPhotoGallery({super.key, required this.photos, required this.onOpen});

  /// URLs de las fotos.
  final List<String> photos;

  /// Se llama con la posición de la foto tocada.
  final ValueChanged<int> onOpen;

  /// Abre el visor a pantalla completa en [initialIndex].
  static Future<void> showViewer(
    BuildContext context, {
    required List<String> photos,
    int initialIndex = 0,
  }) {
    return Navigator.of(context).push<void>(
      MaterialPageRoute<void>(
        fullscreenDialog: true,
        builder: (_) =>
            RtPhotoViewer(photos: photos, initialIndex: initialIndex),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    if (photos.isEmpty) {
      return const SizedBox.shrink();
    }

    final theme = Theme.of(context);
    final colors = theme.colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          'Fotos (${photos.length})',
          style: theme.textTheme.labelLarge!.copyWith(
            color: colors.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: RtSpacing.s2),
        SizedBox(
          height: RtSize.thumbLg,
          child: ListView.separated(
            scrollDirection: Axis.horizontal,
            itemCount: photos.length,
            separatorBuilder: (_, _) => const SizedBox(width: RtSpacing.s2),
            itemBuilder: (context, index) {
              return Semantics(
                button: true,
                label: 'Abrir foto ${index + 1} de ${photos.length}',
                child: InkWell(
                  key: ValueKey<String>('gallery_photo_$index'),
                  onTap: () => onOpen(index),
                  child: ExcludeSemantics(
                    child: RtPhotoThumb(
                      url: photos[index],
                      size: RtPhotoThumbSize.large,
                    ),
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }
}

/// Visor a pantalla completa de [RtPhotoGallery]: fondo `ink`, contador
/// "2 / 3", zoom y deslizamiento. Se cierra con la X o el gesto de volver.
class RtPhotoViewer extends StatefulWidget {
  const RtPhotoViewer({super.key, required this.photos, this.initialIndex = 0});

  final List<String> photos;
  final int initialIndex;

  @override
  State<RtPhotoViewer> createState() => _RtPhotoViewerState();
}

class _RtPhotoViewerState extends State<RtPhotoViewer> {
  late final PageController _controller;
  late int _index;

  @override
  void initState() {
    super.initState();
    _index = widget.initialIndex.clamp(0, widget.photos.length - 1);
    _controller = PageController(initialPage: _index);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    // Sobre el fondo `ink` los íconos y el texto van en `inverse-ink`.
    final onInk = colors.onInverseSurface;

    return ColoredBox(
      color: colors.onSurface,
      child: SafeArea(
        child: Column(
          children: <Widget>[
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s2),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Theme(
                    data: theme.copyWith(
                      colorScheme: colors.copyWith(onSurface: onInk),
                    ),
                    child: RtIconButton(
                      icon: Icons.close_rounded,
                      contentDescription: 'Cerrar',
                      variant: RtIconButtonVariant.ink,
                      onClick: () => Navigator.of(context).maybePop(),
                    ),
                  ),
                  Text(
                    '${_index + 1} / ${widget.photos.length}',
                    style: theme.textTheme.labelLarge!.copyWith(
                      color: onInk,
                      decoration: TextDecoration.none,
                    ),
                  ),
                  const SizedBox(width: RtSize.touchTarget),
                ],
              ),
            ),
            Expanded(
              child: PageView.builder(
                controller: _controller,
                itemCount: widget.photos.length,
                onPageChanged: (value) => setState(() => _index = value),
                itemBuilder: (context, index) {
                  return InteractiveViewer(
                    maxScale: 4,
                    child: Center(
                      child: Image.network(
                        widget.photos[index],
                        fit: BoxFit.contain,
                        errorBuilder: (_, _, _) => Icon(
                          Icons.broken_image_rounded,
                          size: RtSize.stateIconGlyph,
                          color: onInk,
                          semanticLabel: 'La foto no cargó',
                        ),
                      ),
                    ),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
