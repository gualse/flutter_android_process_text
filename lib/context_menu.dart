import 'dart:math';

import 'package:flutter/material.dart';

Widget customContextMenuBuilder(
  BuildContext context,
  EditableTextState editableTextState,
  List<String> items,
  Future<String?> Function(int index, String value, bool readonly) callback,
) {
  var buttonItems = editableTextState.contextMenuButtonItems;
  var textEditingValue = editableTextState.textEditingValue;
  var selection = textEditingValue.selection;

  if (!selection.isCollapsed) {
    var value = selection.textInside(textEditingValue.text);
    for (var i = 0; i < items.length; i++) {
      buttonItems.add(ContextMenuButtonItem(
        onPressed: () async {
          editableTextState.hideToolbar();
          var readOnly = editableTextState.widget.readOnly;
          var newValue = await callback(i, value, readOnly);

          int offset = max(selection.baseOffset, selection.extentOffset);
          var newTextEditingValue = textEditingValue.copyWith(
            selection: TextSelection.collapsed(offset: offset),
          );
          if (!readOnly && newValue != null) {
            newTextEditingValue = newTextEditingValue.replaced(
              selection,
              newValue,
            );
          }

          editableTextState.userUpdateTextEditingValue(
            newTextEditingValue,
            SelectionChangedCause.toolbar,
          );
        },
        label: items[i],
      ));
    }
  }

  return AdaptiveTextSelectionToolbar.buttonItems(
    buttonItems: buttonItems,
    anchors: editableTextState.contextMenuAnchors,
  );
}
