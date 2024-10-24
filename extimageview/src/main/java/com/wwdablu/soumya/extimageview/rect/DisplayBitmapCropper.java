package com.wwdablu.soumya.extimageview.rect;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.wwdablu.soumya.extimageview.ResultCallBack;

final class DisplayBitmapCropper implements Runnable {

    private Bitmap mDisplayedBitmap;
    private RectF mFrameRect;
    private ResultCallBack<Bitmap> mResultCallBack;

    DisplayBitmapCropper(Bitmap mDisplayedBitmap, RectF mFrameRect, ResultCallBack<Bitmap> mResultCallBack) {
        this.mDisplayedBitmap = mDisplayedBitmap;
        this.mFrameRect = mFrameRect;
        this.mResultCallBack = mResultCallBack;
    }

    @Override
    public void run() {

        if(mDisplayedBitmap == null || mDisplayedBitmap.isRecycled()) {
            mResultCallBack.onComplete(null);
            return;
        }

        try {
            int iFrameLeft = (int) Math.floor(mFrameRect.left);
            int iFrameTop = (int) Math.floor(mFrameRect.top);
            int iFrameRight = (int) Math.floor(mFrameRect.right);
            int iFrameBottom = (int) Math.floor(mFrameRect.bottom);

            int iFrameWidth = (int) Math.floor(mFrameRect.width());
            int iFrameHeight = (int) Math.floor(mFrameRect.height());

            Bitmap cropBitmap = Bitmap.createBitmap(iFrameWidth, iFrameHeight, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(cropBitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            canvas.drawBitmap(mDisplayedBitmap,
                    new Rect(iFrameLeft, iFrameTop, iFrameRight, iFrameBottom),
                    new Rect(0, 0, iFrameWidth, iFrameHeight),
                    paint);

            mResultCallBack.onComplete(cropBitmap);

        } catch (Exception ex) {
            mResultCallBack.onError(ex);
        }
    }
}
