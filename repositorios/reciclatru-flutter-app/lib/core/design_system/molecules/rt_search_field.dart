import 'package:flutter/material.dart';

import '../atoms/rt_spinner.dart';
import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import '../theme/rt_theme.dart';

/// Buscador en forma de píldora que flota sobre el mapa para ir a una dirección.
///
/// Las sugerencias las entrega la feature: el componente solo muestra hasta
/// cuatro, avisa cuál se eligió y muestra el error del geocoder. La feature es
/// la que hace el debounce y llama al geocoder; el organismo no lo conoce.
class RtSearchField extends StatefulWidget {
  const RtSearchField({
    super.key,
    required this.query,
    required this.onQueryChange,
    required this.onSearch,
    this.error,
    this.suggestions = const <String>[],
    this.suggestionsLoading = false,
    this.onSuggestionSelected,
  });

  final String query;
  final ValueChanged<String> onQueryChange;
  final VoidCallback onSearch;

  /// Texto bajo el buscador, por ejemplo "No encontramos esa dirección. Mueve el mapa a mano.".
  final String? error;

  /// Direcciones sugeridas por la feature; el componente muestra hasta cuatro.
  final List<String> suggestions;

  /// Muestra el spinner mientras la feature consulta al geocoder.
  final bool suggestionsLoading;

  /// Se llama al elegir una sugerencia; la feature mueve el mapa.
  final ValueChanged<String>? onSuggestionSelected;

  @override
  State<RtSearchField> createState() => _RtSearchFieldState();
}

class _RtSearchFieldState extends State<RtSearchField> {
  static const int _maxSuggestions = 4;
  static const double _suggestionsFallbackHeight = RtSize.touchTarget * 4;

  late final TextEditingController _controller;
  late final FocusNode _focusNode;
  final GlobalKey _fieldKey = GlobalKey();

  /// Oculta las sugerencias viejas mientras llegan las de la nueva consulta.
  bool _suppressSuggestions = false;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.query);
    _focusNode = FocusNode();
    _focusNode.addListener(_handleFocusChange);
  }

  @override
  void didUpdateWidget(covariant RtSearchField oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.query != widget.query && _controller.text != widget.query) {
      _controller.value = TextEditingValue(
        text: widget.query,
        selection: TextSelection.collapsed(offset: widget.query.length),
      );
    }
    if (oldWidget.suggestions != widget.suggestions) {
      _suppressSuggestions = false;
    }
  }

  @override
  void dispose() {
    _focusNode.removeListener(_handleFocusChange);
    _focusNode.dispose();
    _controller.dispose();
    super.dispose();
  }

  void _handleFocusChange() {
    if (_focusNode.hasFocus) {
      _suppressSuggestions = false;
    }
    setState(() {});
  }

  void _handleQueryChanged(String value) {
    setState(() => _suppressSuggestions = true);
    widget.onQueryChange(value);
  }

  void _handleSuggestionSelected(String suggestion) {
    // El padre decide qué mostrar después; el mapa limpia la consulta porque el
    // punto del pin es la fuente de verdad.
    setState(() => _suppressSuggestions = true);
    _focusNode.unfocus();
    widget.onSuggestionSelected?.call(suggestion);
  }

  double _suggestionsAvailableHeight(BuildContext context) {
    final media = MediaQuery.of(context);
    final box = _fieldKey.currentContext?.findRenderObject() as RenderBox?;
    if (box == null || !box.hasSize) {
      return _suggestionsFallbackHeight;
    }
    final fieldBottom = box.localToGlobal(Offset.zero).dy + box.size.height;
    return media.size.height -
        media.viewInsets.bottom -
        fieldBottom -
        RtSpacing.s2;
  }

  double _suggestionsMaxHeight(BuildContext context) {
    return _suggestionsAvailableHeight(context)
        .clamp(RtSize.touchTarget, _suggestionsFallbackHeight)
        .toDouble();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final extra = theme.extension<RtExtraColors>()!;
    final hasError = widget.error != null && widget.error!.isNotEmpty;
    final borderColor = hasError
        ? colors.error
        : _focusNode.hasFocus
        ? colors.primary
        : colors.outline;

    final textStyle = theme.textTheme.bodyLarge!.copyWith(
      color: colors.onSurface,
    );

    final visibleSuggestions = widget.suggestions
        .take(_maxSuggestions)
        .toList(growable: false);
    final showSuggestions =
        _focusNode.hasFocus &&
        _controller.text.trim().isNotEmpty &&
        visibleSuggestions.isNotEmpty &&
        !_suppressSuggestions &&
        _suggestionsAvailableHeight(context) >= RtSize.touchTarget;

    final field = Row(
      children: <Widget>[
        Icon(
          Icons.search_rounded,
          size: RtSize.iconMd,
          color: colors.onSurfaceVariant,
        ),
        const SizedBox(width: RtSpacing.s2),
        Expanded(
          child: TextField(
            controller: _controller,
            focusNode: _focusNode,
            onChanged: _handleQueryChanged,
            onSubmitted: (_) => widget.onSearch(),
            // El toque en una sugerencia está fuera del TextField; sin esto,
            // el campo se desenfoca, la lista desaparece y el tap no llega.
            onTapOutside: (_) {},
            textInputAction: TextInputAction.search,
            maxLines: 1,
            style: textStyle,
            cursorColor: colors.primary,
            decoration: InputDecoration.collapsed(
              hintText: 'Busca una calle o lugar',
              hintStyle: textStyle.copyWith(color: extra.inkSubtle),
            ),
          ),
        ),
        if (widget.suggestionsLoading) ...<Widget>[
          const SizedBox(width: RtSpacing.s2),
          const RtSpinner(),
        ],
      ],
    );

    return TapRegion(
      onTapOutside: (_) {
        _focusNode.unfocus();
        if (_suppressSuggestions) {
          return;
        }
        setState(() => _suppressSuggestions = true);
      },
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: <Widget>[
          Container(
            key: _fieldKey,
            height: RtSize.inputHeight,
            padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s4),
            decoration: BoxDecoration(
              color: colors.surface,
              borderRadius: BorderRadius.circular(RtRadii.full),
              border: Border.all(color: borderColor, width: RtStroke.control),
              boxShadow: RtElevation.level2,
            ),
            alignment: Alignment.center,
            child: field,
          ),
          if (showSuggestions)
            Padding(
              padding: const EdgeInsets.only(top: RtSpacing.s1),
              child: _buildSuggestions(
                context,
                colors,
                textStyle,
                visibleSuggestions,
              ),
            ),
          if (hasError) ...<Widget>[
            const SizedBox(height: RtSpacing.s1),
            Row(
              children: <Widget>[
                Icon(
                  Icons.error_rounded,
                  size: RtSize.iconXs,
                  color: colors.error,
                ),
                const SizedBox(width: RtSpacing.s1),
                Expanded(
                  child: Text(
                    widget.error!,
                    style: theme.textTheme.labelMedium!.copyWith(
                      color: colors.error,
                    ),
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSuggestions(
    BuildContext context,
    ColorScheme colors,
    TextStyle textStyle,
    List<String> suggestions,
  ) {
    final maxHeight = _suggestionsMaxHeight(context);

    return Container(
      constraints: BoxConstraints(maxHeight: maxHeight),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(RtRadii.md),
        border: Border.all(
          color: colors.outlineVariant,
          width: RtStroke.divider,
        ),
        boxShadow: RtElevation.level2,
      ),
      child: Material(
        color: colors.surface,
        borderRadius: BorderRadius.circular(RtRadii.md),
        clipBehavior: Clip.antiAlias,
        child: ListView.separated(
          shrinkWrap: true,
          padding: EdgeInsets.zero,
          physics: const ClampingScrollPhysics(),
          itemCount: suggestions.length,
          separatorBuilder: (_, _) =>
              Container(height: RtStroke.divider, color: colors.outlineVariant),
          itemBuilder: (context, index) {
            final suggestion = suggestions[index];
            return InkWell(
              onTap: () => _handleSuggestionSelected(suggestion),
              child: SizedBox(
                height: RtSize.touchTarget,
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: RtSpacing.s4),
                  child: Row(
                    children: <Widget>[
                      Icon(
                        Icons.location_on_rounded,
                        size: RtSize.iconMd,
                        color: colors.onSurfaceVariant,
                      ),
                      const SizedBox(width: RtSpacing.s2),
                      Expanded(
                        child: Text(
                          suggestion,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: textStyle,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
