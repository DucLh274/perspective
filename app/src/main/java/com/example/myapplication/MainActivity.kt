package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.wwdablu.soumya.extimageview.ResultCallBack
import com.wwdablu.soumya.extimageview.trapez.ExtTrapezImageView


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

        demoTrapez()

    }


    private fun demoTrapez() {


        val extTrapezImageView = findViewById<ExtTrapezImageView>(R.id.ivOrigin)




        Glide.with(this).asBitmap().load(R.drawable.img_test).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
                extTrapezImageView.setImageBitmap(bitmap)
            }
        })
        findViewById<View>(R.id.btnOK).setOnClickListener { v: View? ->
            extTrapezImageView.crop(object : ResultCallBack<Void> {
                override fun onComplete(data: Void?) {
                    extTrapezImageView.getCroppedBitmap(object : ResultCallBack<Bitmap?> {
                        override fun onComplete(data: Bitmap?) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Okkkkkk",
                                    Toast.LENGTH_SHORT
                                ).show()
//                                (findViewById<View>(R.id.ivResult) as AppCompatImageView)
//                                    .setImageBitmap(data)
                            }
                        }

                        override fun onError(throwable: Throwable) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Could not get cropped bitmap" + throwable.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    })
                }

                override fun onError(throwable: Throwable?) {
                }
            })
        }


    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

}