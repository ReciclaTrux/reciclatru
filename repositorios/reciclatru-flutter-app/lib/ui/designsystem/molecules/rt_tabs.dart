/// Define el componente reutilizable rt tabs del sistema visual compartido por las pantallas.
import 'package:flutter/material.dart';

/// Definición de pestaña con contador opcional.
class RtTab {
  const RtTab({required this.label, this.count});
  final String label;
  final int? count;
}

/// Pestañas horizontales con indicador inferior en la seleccionada.
class RtTabs extends StatelessWidget {
  const RtTabs({required this.tabs, required this.selectedIndex, required this.onSelect, super.key});
  final List<RtTab> tabs;
  final int selectedIndex;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) => Column(children: [
    Row(children: [
      for (var index = 0; index < tabs.length; index++)
        Expanded(child: InkWell(
          onTap: () => onSelect(index),
          child: Column(children: [
            SizedBox(height: 48, child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              Text(tabs[index].label, style: TextStyle(
                color: index == selectedIndex ? Theme.of(context).colorScheme.primary : null,
                fontWeight: FontWeight.w600,
              )),
              if ((tabs[index].count ?? 0) > 0) ...[
                const SizedBox(width: 8),
                Text('${tabs[index].count}'),
              ],
            ])),
            Container(height: 2, color: index == selectedIndex ? Theme.of(context).colorScheme.primary : Colors.transparent),
          ]),
        )),
    ]),
    const Divider(height: 1),
  ]);
}
