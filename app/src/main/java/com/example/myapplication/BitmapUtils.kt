package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Point
import kotlin.math.max


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

fun trapezoidToRectangleTransform(
    originalBitmap: Bitmap,
    anchorPoints : List<Point>
): Bitmap? {
    val src = FloatArray(8)

    var point: Point = anchorPoints.get(0)
    src[0] = point.x.toFloat()
    src[1] = point.y.toFloat()

    point = anchorPoints.get(1)
    src[2] = point.x.toFloat()
    src[3] = point.y.toFloat()

    point = anchorPoints.get(3)
    src[4] = point.x.toFloat()
    src[5] = point.y.toFloat()

    point = anchorPoints.get(2)
    src[6] = point.x.toFloat()
    src[7] = point.y.toFloat()

    // set up a dest polygon which is just a rectangle
    val dst = FloatArray(8)
    dst[0] = 0f
    dst[1] = 0f
    dst[2] = originalBitmap.getWidth().toFloat()
    dst[3] = 0f
    dst[4] = originalBitmap.getWidth().toFloat()
    dst[5] = originalBitmap.getHeight().toFloat()
    dst[6] = 0f
    dst[7] = originalBitmap.getHeight().toFloat()

    // create a matrix for transformation.
    val matrix = Matrix()

    // set the matrix to map the source values to the dest values.
    val mapped = matrix.setPolyToPoly(src, 0, dst, 0, 4)

    val mappedTL = floatArrayOf(0f, 0f)
    matrix.mapPoints(mappedTL)
    val maptlx = Math.round(mappedTL[0])
    val maptly = Math.round(mappedTL[1])

    val mappedTR = floatArrayOf(originalBitmap.getWidth().toFloat(), 0f)
    matrix.mapPoints(mappedTR)
    val maptry = Math.round(mappedTR[1])

    val mappedLL = floatArrayOf(0f, originalBitmap.getHeight().toFloat())
    matrix.mapPoints(mappedLL)
    val mapllx = Math.round(mappedLL[0])

    val shiftX = max(-maptlx.toDouble(), -mapllx.toDouble()).toInt()
    val shiftY = max(-maptry.toDouble(), -maptly.toDouble()).toInt()

    var croppedAndCorrected: Bitmap? = null
    if (mapped) {
        val imageOut = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.getWidth(),
            originalBitmap.getHeight(),
            matrix,
            true
        )
        croppedAndCorrected = Bitmap.createBitmap(
            imageOut,
            shiftX,
            shiftY,
            originalBitmap.getWidth(),
            originalBitmap.getHeight(),
            null,
            true
        )
        imageOut.recycle()
        originalBitmap.recycle()
    }

    return croppedAndCorrected
}