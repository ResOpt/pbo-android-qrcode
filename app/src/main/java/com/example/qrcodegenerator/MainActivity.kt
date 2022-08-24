package com.example.qrcodegenerator

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.drawToBitmap
import com.google.android.material.textfield.TextInputEditText
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.URL
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.util.Log;



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val inputData = findViewById<TextInputEditText>(R.id.inp)
        val about = findViewById<ImageView>(R.id.about_btn)
        val back = findViewById<ImageView>(R.id.back_btn)
        val backNav = findViewById<RelativeLayout>(R.id.back_nav)
        val img = findViewById<ImageView>(R.id.img)
        val button = findViewById<Button>(R.id.button)
        val btnSave = findViewById<Button>(R.id.buttonSave)
        val btnReset = findViewById<Button>(R.id.button2)
        val saveTutor = findViewById<TextView>(R.id.savetutor)
        val context = this.applicationContext
        val opts = arrayOf("Hitam", "Merah", "Biru", "Hijau", "Oranye")
        val show = findViewById<TextView>(R.id.textShow)
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
                            show.visibility=View.VISIBLE
                            saveTutor.visibility=View.VISIBLE
                            saveTutor.setText("Apabila Tombol Save Tidak Berfungsi : \n - Beri izin pada penyimpanan  \n - Info Aplikasi>Perizinan>penyimpanan");
                            show.setText(inputData.text)
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
                saveTutor.visibility=View.GONE
                inputData.setText("")
                show.visibility=View.GONE
            }
        })

        show.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                    val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val myClip: ClipData = ClipData.newPlainText("Label", inputData.text)
                    myClipboard.setPrimaryClip(myClip)
                val t: CharSequence = "Text Telah Disalin"
                Toast.makeText(context, t, Toast.LENGTH_SHORT).show()

            }
        })
        about.setOnClickListener{
            val i = Intent(this,About::class.java)
            startActivity(i)
        }
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

