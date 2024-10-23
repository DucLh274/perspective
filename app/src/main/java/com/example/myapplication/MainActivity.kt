package com.example.myapplication

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        test()


    }


    private fun test() {
        val inputBitmap = BitmapFactory.decodeResource(resources, R.drawable.img_test)
        val srcPoints = floatArrayOf(
            0f,
            0f,
            inputBitmap.width.toFloat(),
            0f,
            inputBitmap.width.toFloat(),
            inputBitmap.height.toFloat(),
            0f,
            inputBitmap.height.toFloat()
        )
        val dstPoints = floatArrayOf(
            500f,
            50f,
            inputBitmap.width.toFloat() - 50f,
            50f,
            inputBitmap.width.toFloat() - 200f,
            inputBitmap.height.toFloat() - 200f,
            100f,
            inputBitmap.height.toFloat() - 200f
        )
        val outputBitmap = warpPerspective(inputBitmap, srcPoints, dstPoints)
        val imageViewOrigin = findViewById<ImageView>(R.id.imageOrigin)
        val imageViewResult = findViewById<ImageView>(R.id.imageResult)
        imageViewOrigin.setImageBitmap(inputBitmap)
        imageViewResult.setImageBitmap(outputBitmap)
    }

}