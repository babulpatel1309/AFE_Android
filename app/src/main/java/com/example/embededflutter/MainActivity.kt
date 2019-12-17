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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    private fun init() {
        context = this



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

    private fun sendNumbersToFlutter(firstNumber: Int, secondNumber: Int) {
        val json = JSONObject()
        json.put("first", firstNumber)
        json.put("second", secondNumber)

        startActivityForResult(
            Intent(context, CustomConnection::class.java).putExtra(
                "data",
                json.toString()
            ), 100
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val result = data?.extras?.getInt("result")
            txtCalculations.text = result.toString()
        }
    }
}
