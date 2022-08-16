package com.example.qrcodegenerator

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.content.ContentResolver
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val inputData = findViewById<TextInputEditText>(R.id.inp)
        val img = findViewById<ImageView>(R.id.img)
        val button = findViewById<Button>(R.id.button)
        val btnSave = findViewById<Button>(R.id.buttonSave)
        val btnReset = findViewById<Button>(R.id.button2)
        val context = this.applicationContext
        val opts = arrayOf("Hitam", "Merah", "Biru", "Hijau", "Oranye")

        val dropDown = findViewById<Spinner>(R.id.spinner)

        var color = "black"

        dropDown.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, opts)

        dropDown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val c = opts.get(p2)

                when (c) {
                    "Hitam" -> color = "black"
                    "Merah" -> color = "red"
                    "Biru" -> color = "blue"
                    "Oranye" -> color = "orange"
                    "Hijau" -> color = "green"
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }

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
                            dropDown.visibility = View.GONE
                            btnReset.visibility = View.VISIBLE
                            inputData.visibility = View.GONE
                            button.visibility =View.GONE
                            btnSave.visibility=View.VISIBLE
                            btnSave.setOnClickListener(object: View.OnClickListener {
                                override fun onClick(p0: View?) {
                                    saveMediaToStorage(img.drawToBitmap(), context)
                                }
                            })
                        })

                    }

                    override fun onFailure(call: Call, e: IOException) {
                        println(e.message.toString())
                    }
                })

            }

        })

        btnReset.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                img.visibility = View.GONE
                inputData.visibility = View.VISIBLE
                button.visibility =View.VISIBLE
                btnSave.visibility = View.GONE
                btnReset.visibility = View.GONE
                dropDown.visibility=View.VISIBLE
            }
        })

        }
    }

//    private fun saveImage(bitmap: Bitmap, title:String) {
////        val savedImageURL = MediaStore.Images.Media.insertImage(
////            contentResolver,
////            bitmap,
////            title,
////            "Image of $title"
////        )
//        val contentValues = ContentValues().apply {
//            put(MediaStore.MediaColumns.DISPLAY_NAME, System.currentTimeMillis().toString())
//            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //this one
//                put(MediaStore.Images.ImageColumns.B)
//                put(MediaStore.MediaColumns.IS_PENDING, 1)
//            }
//        }
//    }

fun saveMediaToStorage(bitmap: Bitmap, context: Context?) {
    //Generating a file name
    val filename = "${System.currentTimeMillis()}.jpg"

    //Output stream
    var fos: OutputStream? = null

    //For devices running android >= Q
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        //getting the contentResolver
        context?.contentResolver?.also { resolver ->

            //Content resolver will process the contentvalues
            val contentValues = ContentValues().apply {

                //putting file information in content values
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }

            //Inserting the contentValues to contentResolver and getting the Uri
            val imageUri: Uri? =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            //Opening an outputstream with the Uri that we got
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        //These for devices running on android < Q
        //So I don't think an explanation is needed here
        val imagesDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }

    fos?.use {
        //Finally writing the bitmap to the output stream that we opened
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        val t: CharSequence = "QR telah tersimpan"
        Toast.makeText(context, t, Toast.LENGTH_SHORT).show()
    }
}

