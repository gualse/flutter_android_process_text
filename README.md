# flutter_android_process_text
This project is an attempt to add Android's PROCESS_TEXT context menu buttons into flutter.

See these files:
1. [AndroidManifest.xml](android/app/src/main/AndroidManifest.xml)
2. [MainActivity.kt](android/app/src/main/kotlin/com/example/androidflutterselect/MainActivity.kt)
3. [context_menu.dart](lib/context_menu.dart)
4. [main.dart](lib/main.dart)

There are no extra dependencies but you can use a [share_plus](https://pub.dev/packages/share_plus) package instead native implementation. Also, the share button is untranslateable here.
