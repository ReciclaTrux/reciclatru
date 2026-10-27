import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import '../theme/rt_colors.dart';
import '../theme/rt_radii.dart';
import '../theme/rt_spacing.dart';
import 'rt_icon_button.dart';

/// Campo de texto con etiqueta arriba y ayuda o error abajo.
///
/// Variantes del Release 1: correo, contraseña (con botón para mostrar u
/// ocultar), celular, peso con sufijo, texto largo con contador y solo lectura.
///
/// Es un [StatefulWidget] solo por estado de UI (controlador, foco y
/// visibilidad de la contraseña); el valor de negocio lo posee quien recibe
/// [onValueChange].
class RtTextField extends StatefulWidget {
  const RtTextField({
    super.key,
    required this.label,
    required this.value,
    required this.onValueChange,
    this.placeholder,
    this.helper,
    this.error,
    this.leadingIcon,
    this.suffix,
    this.maxLength,
    this.keyboardType,
    this.readOnly = false,
    this.obscureText = false,
    this.maxLines = 1,
  });

  final String label;
  final String value;
  final ValueChanged<String> onValueChange;
  final String? placeholder;
  final String? helper;

  /// Si existe, reemplaza visualmente a [helper].
  final String? error;
  final IconData? leadingIcon;

  /// Texto al final del campo, por ejemplo "kg".
  final String? suffix;

  /// Muestra el contador y limita la entrada a esta cantidad de caracteres.
  final int? maxLength;
  final TextInputType? keyboardType;
  final bool readOnly;

  /// Contraseña: oculta el valor y muestra el botón para revelarlo.
  final bool obscureText;

  /// Mayor a 1 convierte el campo en área de texto.
  final int maxLines;

  @override
  State<RtTextField> createState() => _RtTextFieldState();
}

class _RtTextFieldState extends State<RtTextField> {
  late final TextEditingController _controller;
  late final FocusNode _focusNode;
  late bool _obscured;

  @override
  void initState() {
    super.initState();
    _controller = TextEditingController(text: widget.value);
    _focusNode = FocusNode();
    _obscured = widget.obscureText;
    _focusNode.addListener(_handleFocusChange);
  }

  @override
  void didUpdateWidget(covariant RtTextField oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.value != widget.value && _controller.text != widget.value) {
      _controller.value = TextEditingValue(
        text: widget.value,
        selection: TextSelection.collapsed(offset: widget.value.length),
      );
    }
    if (oldWidget.obscureText != widget.obscureText) {
      _obscured = widget.obscureText;
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
    setState(() {});
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final colors = theme.colorScheme;
    final extra = theme.extension<RtExtraColors>()!;
    final hasError = widget.error != null && widget.error!.isNotEmpty;
    final isMultiline = widget.maxLines > 1;
    final isFocused = _focusNode.hasFocus;

    final borderColor = hasError
        ? colors.error
        : isFocused
        ? colors.primary
        : widget.readOnly
        ? colors.outlineVariant
        : colors.outline;

    final textStyle = theme.textTheme.bodyLarge!.copyWith(
      color: colors.onSurface,
    );
    final helperStyle = theme.textTheme.labelMedium!.copyWith(
      color: colors.onSurfaceVariant,
    );

    final suffix = _buildSuffix(theme, colors);

    final field = TextField(
      controller: _controller,
      focusNode: _focusNode,
      onChanged: widget.onValueChange,
      readOnly: widget.readOnly,
      obscureText: _obscured,
      enableSuggestions: !_obscured,
      autocorrect: !_obscured,
      keyboardType: widget.keyboardType,
      maxLines: _obscured ? 1 : widget.maxLines,
      minLines: !_obscured && isMultiline ? widget.maxLines : null,
      textAlignVertical: isMultiline
          ? TextAlignVertical.top
          : TextAlignVertical.center,
      style: textStyle,
      cursorColor: colors.primary,
      inputFormatters: widget.maxLength == null
          ? null
          : <TextInputFormatter>[
              LengthLimitingTextInputFormatter(widget.maxLength),
            ],
      decoration: InputDecoration.collapsed(
        hintText: widget.placeholder,
        hintStyle: textStyle.copyWith(color: extra.inkSubtle),
      ),
    );

    final input = Container(
      constraints: BoxConstraints(
        minHeight: isMultiline ? RtSize.inputAreaMinHeight : RtSize.inputHeight,
      ),
      padding: EdgeInsets.only(
        left: RtSpacing.s4,
        right: RtSpacing.s2,
        top: isMultiline ? RtSpacing.s3 : 0,
        bottom: isMultiline ? RtSpacing.s3 : 0,
      ),
      decoration: BoxDecoration(
        color: widget.readOnly
            ? colors.surfaceContainerHighest
            : colors.surface,
        borderRadius: BorderRadius.circular(RtRadii.md),
        border: Border.all(color: borderColor, width: RtStroke.control),
        boxShadow: hasError || isFocused
            ? <BoxShadow>[
                BoxShadow(
                  color: borderColor,
                  spreadRadius: RtStroke.thin,
                  blurRadius: 0,
                ),
              ]
            : null,
      ),
      child: Row(
        crossAxisAlignment: isMultiline
            ? CrossAxisAlignment.start
            : CrossAxisAlignment.center,
        children: <Widget>[
          if (widget.leadingIcon != null) ...<Widget>[
            Icon(
              widget.leadingIcon,
              size: RtSize.iconMd,
              color: colors.onSurfaceVariant,
            ),
            const SizedBox(width: RtSpacing.s2),
          ],
          Expanded(
            child: Semantics(label: widget.label, child: field),
          ),
          if (suffix != null) ...<Widget>[
            const SizedBox(width: RtSpacing.s2),
            suffix,
          ],
        ],
      ),
    );

    final counter = widget.maxLength == null
        ? null
        : '${widget.value.length}/${widget.maxLength}';
    final hasHelp = hasError || widget.helper != null || counter != null;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: <Widget>[
        Text(
          widget.label,
          style: theme.textTheme.labelLarge!.copyWith(color: colors.onSurface),
        ),
        const SizedBox(height: RtSpacing.fieldLabelGap),
        input,
        if (hasHelp) ...<Widget>[
          const SizedBox(height: RtSpacing.fieldLabelGap),
          _buildHelp(
            colors: colors,
            helperStyle: helperStyle,
            counter: counter,
          ),
        ],
      ],
    );
  }

  Widget? _buildSuffix(ThemeData theme, ColorScheme colors) {
    if (widget.readOnly) {
      return Icon(
        Icons.lock_rounded,
        size: RtSize.iconMd,
        color: colors.onSurfaceVariant,
      );
    }
    if (widget.obscureText) {
      return RtIconButton(
        icon: _obscured
            ? Icons.visibility_rounded
            : Icons.visibility_off_rounded,
        contentDescription: _obscured
            ? 'Mostrar contraseña'
            : 'Ocultar contraseña',
        onClick: () => setState(() => _obscured = !_obscured),
      );
    }
    if (widget.suffix != null) {
      return Padding(
        padding: const EdgeInsets.only(right: RtSpacing.s2),
        child: Text(
          widget.suffix!,
          style: theme.textTheme.bodyLarge!.copyWith(
            fontWeight: FontWeight.w600,
            color: colors.onSurfaceVariant,
          ),
        ),
      );
    }
    return null;
  }

  Widget _buildHelp({
    required ColorScheme colors,
    required TextStyle helperStyle,
    required String? counter,
  }) {
    final hasError = widget.error != null && widget.error!.isNotEmpty;

    if (hasError) {
      return Row(
        children: <Widget>[
          Icon(Icons.error_rounded, size: RtSize.iconXs, color: colors.error),
          const SizedBox(width: RtSpacing.s1),
          Flexible(
            child: Text(
              widget.error!,
              style: helperStyle.copyWith(color: colors.error),
            ),
          ),
          if (counter != null) ...<Widget>[
            const SizedBox(width: RtSpacing.s2),
            Text(counter, style: helperStyle),
          ],
        ],
      );
    }

    return Row(
      mainAxisAlignment: widget.helper == null
          ? MainAxisAlignment.end
          : MainAxisAlignment.spaceBetween,
      children: <Widget>[
        if (widget.helper != null)
          Flexible(child: Text(widget.helper!, style: helperStyle)),
        if (counter != null) Text(counter, style: helperStyle),
      ],
    );
  }
}
