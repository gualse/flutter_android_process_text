package com.example.androidflutterselect

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {

    private val actionsList = ArrayList<ResolveInfo>()
    private val callbackMap = HashMap<Int, MethodChannel.Result>()

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
                    val callbackCode = result.hashCode()
                    callbackMap[callbackCode] = result
                    processTextAction(
                        call.argument("value") ?: "",
                        call.argument("id") ?: -1,
                        call.argument("readonly") ?: true,
                        callbackCode
                    )
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

    private fun processTextAction(value: String, id: Int, readonly: Boolean, callbackCode: Int) {
        if (id == 0) { // Share button
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, value)
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, null))
            returnResult(callbackCode, null)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val info = actionsList.getOrNull(id - 1) ?: return
        val intent = Intent().apply {
            setClassName(info.activityInfo.packageName, info.activityInfo.name)
            action = Intent.ACTION_PROCESS_TEXT
            putExtra(Intent.EXTRA_PROCESS_TEXT, value)
            putExtra(Intent.EXTRA_PROCESS_TEXT_READONLY, readonly)
            type = "text/plain"
        }
        startActivityForResult(intent, callbackCode)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = if (resultCode == Activity.RESULT_OK) {
            data?.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        } else {
            null
        }
        returnResult(requestCode, result)
    }

    private fun returnResult(callbackCode: Int, result: String?) {
        callbackMap.remove(callbackCode)?.success(result)
    }

    companion object {
        private const val CHANNEL = "com.example.androidflutterselect/process_text"
    }
}
