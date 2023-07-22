package com.example.androidflutterselect

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat.startActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.KeyData.CHANNEL
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val actionsList = ArrayList<ResolveInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        val intent = Intent().setAction(Intent.ACTION_PROCESS_TEXT).setType("text/plain")
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        for (item in info) {
            actionsList.add(item)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "textActionList" -> result.success(textActionList())
                "processTextAction" -> {
                    processTextAction(
                        call.argument("value") ?: "",
                        call.argument("id") ?: -1
                    )
                    result.success(null)
                }

                else -> result.notImplemented()
            }
        }
    }

    private fun textActionList() = arrayListOf("Share").apply {
        for (item in actionsList) {
            add(item.loadLabel(packageManager).toString())
        }
    }

    private fun processTextAction(value: String, id: Int) {
        val intent = if (id == 0) {
            val i = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, value)
                type = "text/plain"
            }
            Intent.createChooser(i, null)
        } else {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
            val info = actionsList.getOrNull(id - 1) ?: return
            Intent().apply {
                setClassName(info.activityInfo.packageName, info.activityInfo.name)
                action = Intent.ACTION_PROCESS_TEXT
                putExtra(Intent.EXTRA_PROCESS_TEXT, value)
                putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, true)
                type = "text/plain"
            }
        }

        startActivity(intent)
    }

    companion object {
        private const val CHANNEL = "com.example.androidflutterselect/process_text"
    }
}
