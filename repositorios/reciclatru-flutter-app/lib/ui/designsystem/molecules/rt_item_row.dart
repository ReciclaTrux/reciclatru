/// Define el componente reutilizable rt item row del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

/// Fila editable de material y peso con controladores de texto sincronizados para evitar pérdida de foco o cursor.
class RtItemRow extends StatefulWidget {
  const RtItemRow({
    required this.description,
    required this.onDescriptionChange,
    required this.weight,
    required this.onWeightChange,
    required this.onRemove,
    this.error,
    this.descriptionInvalid = false,
    this.weightInvalid = false,
    super.key,
  });

  final String description;
  final ValueChanged<String> onDescriptionChange;
  final String weight;
  final ValueChanged<String> onWeightChange;
  final VoidCallback onRemove;
  final String? error;
  final bool descriptionInvalid;
  final bool weightInvalid;

  @override
  State<RtItemRow> createState() => _RtItemRowState();
}

class _RtItemRowState extends State<RtItemRow> {
  late final TextEditingController _descController;
  late final TextEditingController _weightController;

  @override
  void initState() {
    super.initState();
    _descController = TextEditingController(text: widget.description);
    _weightController = TextEditingController(text: widget.weight);
  }

  @override
  void didUpdateWidget(covariant RtItemRow oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.description != widget.description && _descController.text != widget.description) {
      _descController.value = TextEditingValue(
        text: widget.description,
        selection: TextSelection.collapsed(offset: widget.description.length),
      );
    }
    if (oldWidget.weight != widget.weight && _weightController.text != widget.weight) {
      _weightController.value = TextEditingValue(
        text: widget.weight,
        selection: TextSelection.collapsed(offset: widget.weight.length),
      );
    }
  }

  @override
  void dispose() {
    _descController.dispose();
    _weightController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Column(
      children: [
        Row(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Expanded(
              flex: 3,
              child: TextField(
                controller: _descController,
                onChanged: widget.onDescriptionChange,
                decoration: InputDecoration(
                  labelText: 'Material',
                  errorText: widget.descriptionInvalid ? 'Revisa el material' : null,
                ),
              ),
            ),
            const SizedBox(width: 8),
            Expanded(
              flex: 2,
              child: TextField(
                controller: _weightController,
                onChanged: widget.onWeightChange,
                keyboardType: const TextInputType.numberWithOptions(decimal: true),
                decoration: InputDecoration(
                  labelText: 'Peso',
                  suffixText: 'kg',
                  errorText: widget.weightInvalid ? 'Revisa el peso' : null,
                ),
              ),
            ),
            IconButton(
              tooltip: 'Quitar ítem',
              onPressed: widget.onRemove,
              icon: const Icon(Icons.delete_outline_rounded),
            ),
          ],
        ),
        if (widget.error != null) ...[
          const SizedBox(height: 4),
          Align(
            alignment: Alignment.centerLeft,
            child: Text(
              widget.error!,
              style: TextStyle(color: colors.error),
            ),
          ),
        ],
      ],
    );
  }
}
