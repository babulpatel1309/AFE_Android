package com.example.embededflutter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.systemchannels.PlatformChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformPlugin
import org.json.JSONObject

class BridgeActivity : AppCompatActivity() {

    private val flutterEngine: FlutterEngine by lazy {
        FlutterEngine(this.applicationContext)
    }

    private val flutterView: FlutterView by lazy {
        FlutterView(this.applicationContext)
    }

    lateinit var platformPlugin: PlatformPlugin

    private var extraData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        extraData = intent?.extras?.getString("data")

        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        flutterView.attachToFlutterEngine(flutterEngine)

        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE, flutterEngine)

        initMethodChannel()

    }


    private fun initMethodChannel() {

        val channel = MethodChannel(flutterEngine.dartExecutor, FLUTTER_CHANNEL)

        channel.setMethodCallHandler { call, result ->
            // manage method calls here
            if (call.method == "FromClientToHost") {
                val resultStr = call.arguments.toString()
                val resultJson = JSONObject(resultStr)
                val res = resultJson.getInt("result")
                val operation = resultJson.getString("operation")

                val intent = Intent()
                intent.putExtra("result", res)
                intent.putExtra("operation", operation)
                setResult(Activity.RESULT_OK, intent)
                flutterEngine.dartExecutor.onDetachedFromJNI()
                finish()
            } else {
                result.notImplemented()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        platformPlugin =
            PlatformPlugin(this@BridgeActivity, PlatformChannel(flutterEngine.dartExecutor))

        startActivityForResult(
            FlutterActivity.withCachedEngine(FLUTTER_ENGINE).build(
                this
            ),
            100
        )

        sendMessage()
    }


    var isSent = false
    private fun sendMessage() {
        Handler().postDelayed({
            if (flutterEngine.renderer.isDisplayingFlutterUi && !isSent) {
                isSent = true
                MethodChannel(
                    flutterEngine.dartExecutor,
                    FLUTTER_CHANNEL
                ).invokeMethod("fromHostToClient", extraData)
            } else {
                print("Retrying")
                sendMessage()
            }

        }, 500)
    }

}