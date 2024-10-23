package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Matrix


/***
Create by ADMIN
Create at 17:16/23-10-2024
 ***/
private const val TAG = "BitmapUtils"

fun warpPerspective(inputBitmap: Bitmap, srcPoints: FloatArray, dstPoints: FloatArray): Bitmap {
    val matrix = Matrix()
    matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)

    return Bitmap.createBitmap(inputBitmap, 0, 0, inputBitmap.width, inputBitmap.height, matrix, true)
}