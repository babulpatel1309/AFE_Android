package com.example.embededflutter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import io.flutter.app.FlutterActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformPlugin
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.view.FlutterMain
import io.flutter.view.FlutterView
import org.json.JSONObject

class CustomConnection : FlutterActivity() {


    lateinit var platformPlugin: PlatformPlugin

    lateinit var customFlutterView: FlutterView
    private var extraData: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        FlutterMain.startInitialization(this.applicationContext)
        super.onCreate(savedInstanceState)

        extraData = intent?.extras?.getString("data")

        customFlutterView = flutterView

        initMethodChannel()
    }

    private fun initMethodChannel() {
        val channel = MethodChannel(flutterView, FLUTTER_CHANNEL)

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
                finish()
            } else {
                result.notImplemented()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        /*startActivityForResult(
            io.flutter.embedding.android.FlutterActivity.withCachedEngine(FLUTTER_ENGINE).build(
                this
            ),
            100
        )*/

        sendMessage()
    }

    var isSent = false
    private fun sendMessage() {
        Handler().postDelayed({
            if (flutterView.hasRenderedFirstFrame() && !isSent) {
                isSent = true
                MethodChannel(
                    flutterView,
                    FLUTTER_CHANNEL
                ).invokeMethod("fromHostToClient", extraData)
            } else {
                print("Retrying")
                sendMessage()
            }

        }, 500)
    }

}