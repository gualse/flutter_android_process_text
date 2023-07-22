import 'package:flutter/material.dart';

Widget customContextMenuBuilder(
  BuildContext context,
  EditableTextState editableTextState,
  List<String> items,
  void Function(int index, String value) callback,
) {
  var buttonItems = editableTextState.contextMenuButtonItems;
  var selection = editableTextState.textEditingValue.selection;

  if (!selection.isCollapsed) {
    var value = selection.textInside(editableTextState.textEditingValue.text);
    for (var i = 0; i < items.length; i++) {
      buttonItems.add(ContextMenuButtonItem(
        onPressed: () => callback(i, value),
        label: items[i],
      ));
    }
  }

  return AdaptiveTextSelectionToolbar.buttonItems(
    buttonItems: buttonItems,
    anchors: editableTextState.contextMenuAnchors,
  );
}
