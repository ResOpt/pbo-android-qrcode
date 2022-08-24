package com.example.qrcodegenerator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

class About : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        val back_btn = findViewById<ImageView>(R.id.back_btn)
        back_btn.setOnClickListener{
            val i = Intent(this,MainActivity::class.java)
            startActivity(i)
        }
        }
    }
