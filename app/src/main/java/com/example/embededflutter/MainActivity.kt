package com.example.embededflutter

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.embedding.engine.systemchannels.PlatformChannel
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformPlugin
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    lateinit var context: Context

    private val flutterEngine: FlutterEngine by lazy {
        FlutterEngine(this.applicationContext)
    }

    private val flutterView: FlutterView by lazy {
        FlutterView(this.applicationContext)
    }

    lateinit var platformPlugin: PlatformPlugin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        context = this

        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault()
        )

        flutterView.attachToFlutterEngine(flutterEngine)

        FlutterEngineCache.getInstance().put(FLUTTER_ENGINE, flutterEngine)

        initMethodChannel()

        btnSend.setOnClickListener {

            val number1 = etFirstNumber.text.trim().toString()
            val number2 = etSecondNumber.text.trim().toString()

            if (number1.isEmpty()) {
                Toast.makeText(context, "Please enter valid first number", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            if (number2.isEmpty()) {
                Toast.makeText(context, "Please enter valid second number", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }


            sendNumbersToFlutter(number1.toInt(), number2.toInt())
        }

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
                finish()
            } else {
                result.notImplemented()
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        platformPlugin =
            PlatformPlugin(this@MainActivity, PlatformChannel(flutterEngine.dartExecutor))

    }

    private fun sendNumbersToFlutter(firstNumber: Int, secondNumber: Int) {

        val json = JSONObject()
        json.put("first", firstNumber)
        json.put("second", secondNumber)
        Handler().postDelayed({
            MethodChannel(
                flutterEngine.dartExecutor,
                FLUTTER_CHANNEL
            ).invokeMethod("fromHostToClient", json.toString())
        }, 500)

        startActivity(
            FlutterActivity.withCachedEngine(FLUTTER_ENGINE).build(
                context
            )
        )
    }
}
