package com.example.qrcodegenerator

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import java.net.URL
import android.view.Window
import android.view.MenuItem;
import android.widget.*
import androidx.core.view.marginTop


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val inputData = findViewById<TextInputEditText>(R.id.inp)
        val desc = findViewById<TextView>(R.id.desc)
        val img = findViewById<ImageView>(R.id.img)
        val colorPick = findViewById<RelativeLayout>(R.id.colorPicker)
        val button = findViewById<Button>(R.id.button)
        val radioGroup = findViewById<RadioGroup>(R.id.radio_group)
        val defaultButton = findViewById<RadioButton>(R.id.buttonBlack)
        val btnSave = findViewById<Button>(R.id.buttonSave)
        defaultButton.isChecked = true
        var color = "black"

        button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                val jsonObject = JSONObject()
                jsonObject.put("data", inputData.text.toString())
                jsonObject.put("color", color)
                val jsonObjectString = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder().url("https://qrcode.resultoption.tech/create")
                    .post(jsonObjectString)
                    .build()

                val client = OkHttpClient();

                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val resp = response.body?.string()

                        val jsonObj = JSONObject(resp)
                        val url = URL(jsonObj.get("data").toString())
                        val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                        runOnUiThread(Runnable {
                            img.setImageBitmap(bitmap)
                            img.visibility = View.VISIBLE
                            desc.visibility = View.VISIBLE
                            desc.setText(inputData.text.toString())
                            inputData.setText("")
                            colorPick.visibility = View.VISIBLE
                            inputData.visibility = View.GONE
                            button.visibility =View.GONE
                            btnSave.visibility=View.VISIBLE
                        })

                    }

                    override fun onFailure(call: Call, e: IOException) {
                        println(e.message.toString())
                    }
                })

            }

        })
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.buttonBlack)
                color = "black"
            if(checkedId == R.id.buttonRed)
                color = "red"
            if(checkedId == R.id.buttonYellow)
                color = "yellow"
            if(checkedId == R.id.buttonGreen)
                color = "green"
            val jsonObject = JSONObject()
            jsonObject.put("data", inputData.text.toString())
            jsonObject.put("color", color)
            val jsonObjectString = jsonObject.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request.Builder().url("https://qrcode.resultoption.tech/create")
                .post(jsonObjectString)
                .build()

            val client = OkHttpClient();

            client.newCall(request).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val resp = response.body?.string()

                    val jsonObj = JSONObject(resp)
                    val url = URL(jsonObj.get("data").toString())
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    runOnUiThread(Runnable {
                        img.setImageBitmap(bitmap)
                        img.visibility = View.VISIBLE
                        inputData.setText("")
                        colorPick.visibility = View.VISIBLE

                    })

                }

                override fun onFailure(call: Call, e: IOException) {
                    println(e.message.toString())
                }
            })
        }
    }
}